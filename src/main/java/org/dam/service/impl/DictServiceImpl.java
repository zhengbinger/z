package org.dam.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.dam.entity.DictData;
import org.dam.entity.DictType;
import org.dam.mapper.DictDataMapper;
import org.dam.mapper.DictTypeMapper;
import org.dam.service.DictService;
import org.dam.vo.DictItemVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典服务实现
 * 读走缓存，写触发 evict
 * 缓存键: dict:{dictCode}:{locale}（如 dict:user_status:zh_CN）
 * 同类自调用通过 @Lazy 自注入保证 @Cacheable 生效
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@Slf4j
@Service
public class DictServiceImpl implements DictService {

    /**
     * 缓存空间名
     */
    public static final String CACHE_SPACE = "dict";

    @Resource
    private DictTypeMapper dictTypeMapper;

    @Resource
    private DictDataMapper dictDataMapper;

    /**
     * 自注入：保证同类内调用 listItemsByCode 时 @Cacheable 仍生效
     */
    @Resource
    @Lazy
    private DictService self;

    /**
     * 按 dictCode 拉取字典项（走缓存）
     * 缓存键: dict:{dictCode}:{locale}
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @Override
    @Cacheable(value = CACHE_SPACE, key = "#p0 + ':' + T(org.springframework.context.i18n.LocaleContextHolder).getLocale().toString()")
    public List<DictItemVO> listItemsByCode(String dictCode) {
        if (StrUtil.isBlank(dictCode)) {
            return Collections.emptyList();
        }
        DictType type = dictTypeMapper.selectOne(
                Wrappers.<DictType>lambdaQuery().eq(DictType::getDictCode, dictCode));
        if (type == null) {
            return Collections.emptyList();
        }
        List<DictData> list = dictDataMapper.selectList(
                Wrappers.<DictData>lambdaQuery()
                        .eq(DictData::getDictTypeId, type.getId())
                        .eq(DictData::getStatus, 1)
                        .orderByAsc(DictData::getSort));
        boolean isEn = isEnLocale();
        return list.stream().map(d -> {
            DictItemVO vo = new DictItemVO();
            vo.setValue(d.getDictValue());
            vo.setLabel(isEn && StrUtil.isNotBlank(d.getDictLabelEn())
                    ? d.getDictLabelEn() : d.getDictLabelZh());
            vo.setCssClass(d.getCssClass());
            vo.setSort(d.getSort());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 反查标签（导出/详情页展示）
     * 通过自注入走缓存，找不到返回原值
     *
     * @param dictCode  字典编码
     * @param dictValue 字典值
     * @return 当前 Locale 下的标签
     */
    @Override
    public String getLabel(String dictCode, String dictValue) {
        if (StrUtil.isBlank(dictCode) || StrUtil.isBlank(dictValue)) {
            return dictValue;
        }
        return self.listItemsByCode(dictCode).stream()
                .filter(vo -> vo.getValue().equals(dictValue))
                .map(DictItemVO::getLabel)
                .findFirst()
                .orElse(dictValue);
    }

    /**
     * 批量反查标签（列表回显避免 N+1 的推荐入口）
     * 一次性按 dictCode 拉取，内存映射
     *
     * @param dictCode 字典编码
     * @return dictValue -> label 映射
     */
    public Map<String, String> labelMap(String dictCode) {
        return self.listItemsByCode(dictCode).stream()
                .collect(Collectors.toMap(DictItemVO::getValue, DictItemVO::getLabel, (a, b) -> a));
    }

    /**
     * 校验入参值合法性
     *
     * @param dictCode  字典编码
     * @param dictValue 字典值
     * @return true 表示合法
     */
    @Override
    public boolean isValidValue(String dictCode, String dictValue) {
        if (StrUtil.isBlank(dictCode) || StrUtil.isBlank(dictValue)) {
            return false;
        }
        return self.listItemsByCode(dictCode).stream()
                .anyMatch(vo -> vo.getValue().equals(dictValue));
    }

    /**
     * 新增/更新字典类型（触发缓存 evict）
     *
     * @param dictType 字典类型
     * @return 字典类型 ID
     */
    @Override
    @CacheEvict(value = CACHE_SPACE, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateType(DictType dictType) {
        if (dictType.getId() == null) {
            log.info("新增字典类型，dictCode={}，dictName={}", dictType.getDictCode(), dictType.getDictNameZh());
            dictTypeMapper.insert(dictType);
        } else {
            log.info("修改字典类型，id={}，dictCode={}", dictType.getId(), dictType.getDictCode());
            dictTypeMapper.updateById(dictType);
        }
        return dictType.getId();
    }

    /**
     * 新增/更新字典项（触发缓存 evict）
     *
     * @param dictData 字典项
     * @return 字典项 ID
     */
    @Override
    @CacheEvict(value = CACHE_SPACE, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateData(DictData dictData) {
        if (dictData.getId() == null) {
            log.info("新增字典项，dictTypeId={}，dictValue={}，dictLabel={}",
                    dictData.getDictTypeId(), dictData.getDictValue(), dictData.getDictLabelZh());
            dictDataMapper.insert(dictData);
        } else {
            log.info("修改字典项，id={}，dictValue={}", dictData.getId(), dictData.getDictValue());
            dictDataMapper.updateById(dictData);
        }
        return dictData.getId();
    }

    /**
     * 判断当前 Locale 是否为英文
     *
     * @return true 表示英文环境
     */
    private boolean isEnLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return "en_US".equals(locale.toString()) || "en".equals(locale.getLanguage());
    }

}
