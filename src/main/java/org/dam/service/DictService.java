package org.dam.service;

import org.dam.entity.DictData;
import org.dam.entity.DictType;
import org.dam.vo.DictItemVO;

import java.util.List;

/**
 * 字典服务接口
 * 读走缓存，写触发 evict
 * 提供字典项查询、标签反查、入参校验、字典类型/项的新增与更新
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
public interface DictService {

    /**
     * 按 dictCode 查询所有启用字典项（前端下拉框用，走缓存）
     * 按 LocaleContextHolder 当前 Locale 选择 zh/en 标签
     *
     * @param dictCode 字典编码
     * @return 字典项列表，无匹配返回空集合
     */
    List<DictItemVO> listItemsByCode(String dictCode);

    /**
     * 按 dictCode + dictValue 反查当前 Locale 下的标签（导出/展示用）
     * 找不到返回原值 dictValue
     *
     * @param dictCode  字典编码
     * @param dictValue 字典值
     * @return 当前 Locale 下的标签
     */
    String getLabel(String dictCode, String dictValue);

    /**
     * 校验 dictValue 是否在字典合法范围内
     *
     * @param dictCode  字典编码
     * @param dictValue 字典值
     * @return true 表示合法
     */
    boolean isValidValue(String dictCode, String dictValue);

    /**
     * 新增/更新字典类型（运营后台用，触发缓存 evict）
     *
     * @param dictType 字典类型
     * @return 字典类型 ID
     */
    Long saveOrUpdateType(DictType dictType);

    /**
     * 新增/更新字典项（触发缓存 evict）
     *
     * @param dictData 字典项
     * @return 字典项 ID
     */
    Long saveOrUpdateData(DictData dictData);

}
