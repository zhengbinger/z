package org.dam.component.dict;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.dam.service.DictService;
import org.dam.vo.DictItemVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典值-标签转换工具
 * 用于数据导出 Excel / 报表 / 详情页等场景，把 dictValue 转为展示标签
 * 不依赖 EasyExcel：未来引入 EasyExcel 时，可基于本类方法实现其 Converter 接口适配
 *
 * <p>用法示例：
 * <pre>
 * // 导出：value -> label
 * String label = dictValueConverter.toLabel("user_status", "0"); // 返回"禁用"
 *
 * // 导入：label -> value
 * String value = dictValueConverter.toValue("user_status", "禁用"); // 返回"0"
 *
 * // 批量转换（列表回显，避免 N+1，一次性拉取内存映射）
 * List<String> labels = dictValueConverter.toLabels("user_status", valueList);
 * </pre>
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@Component
public class DictValueConverter {

    @Resource
    private DictService dictService;

    /**
     * 导出/展示：value → label
     * 找不到返回原值
     *
     * @param dictCode  字典编码
     * @param dictValue 字典值
     * @return 当前 Locale 下的标签
     */
    public String toLabel(String dictCode, String dictValue) {
        return dictService.getLabel(dictCode, dictValue);
    }

    /**
     * 导入：label → value
     * 找不到返回原标签
     *
     * @param dictCode  字典编码
     * @param dictLabel 字典标签
     * @return 字典值
     */
    public String toValue(String dictCode, String dictLabel) {
        if (StrUtil.isBlank(dictCode) || StrUtil.isBlank(dictLabel)) {
            return dictLabel;
        }
        List<DictItemVO> items = dictService.listItemsByCode(dictCode);
        return items.stream()
                .filter(vo -> vo.getLabel().equals(dictLabel))
                .map(DictItemVO::getValue)
                .findFirst()
                .orElse(dictLabel);
    }

    /**
     * 批量 value → label（列表回显推荐入口，一次性拉取内存映射，避免 N+1）
     *
     * @param dictCode 字典编码
     * @param values   字典值列表
     * @return 标签列表，顺序与入参一致
     */
    public List<String> toLabels(String dictCode, List<String> values) {
        if (CollUtil.isEmpty(values)) {
            return Collections.emptyList();
        }
        Map<String, String> labelMap = dictService.listItemsByCode(dictCode).stream()
                .collect(Collectors.toMap(DictItemVO::getValue, DictItemVO::getLabel, (a, b) -> a));
        return values.stream()
                .map(v -> labelMap.getOrDefault(v, v))
                .collect(Collectors.toList());
    }

}
