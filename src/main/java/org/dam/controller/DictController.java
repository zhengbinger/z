package org.dam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.dam.common.response.Result;
import org.dam.component.security.annotation.RequiresPermission;
import org.dam.entity.DictData;
import org.dam.entity.DictType;
import org.dam.service.DictService;
import org.dam.vo.DictItemVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典管理 Controller
 * 提供给前端拉取下拉框数据源，及运营后台维护字典类型/项
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@Slf4j
@RestController
@RequestMapping("/dict")
@Tag(name = "字典管理", description = "字典下拉框数据源 + 字典类型/项维护接口")
public class DictController {

    @Resource
    private DictService dictService;

    /**
     * 按 dictCode 拉取字典项列表
     * 前端下拉框数据源统一入口
     *
     * @param dictCode 字典编码
     * @return 字典项列表
     */
    @GetMapping("/{dictCode}/items")
    @Operation(summary = "拉取单个字典项列表", description = "前端下拉框数据源统一入口，按当前 Accept-Language 切换标签")
    @RequiresPermission("dict:list")
    public Result<List<DictItemVO>> listItems(
            @Parameter(description = "字典编码，如 user_status", required = true)
            @PathVariable String dictCode) {
        List<DictItemVO> items = dictService.listItemsByCode(dictCode);
        return Result.success(items);
    }

    /**
     * 批量拉取多个字典（前端首屏一次性加载）
     *
     * @param dictCodes 字典编码列表
     * @return dictCode -> 字典项列表 映射
     */
    @PostMapping("/items/batch")
    @Operation(summary = "批量拉取字典项", description = "前端首屏一次性加载多个字典，避免 N 个并发请求")
    @RequiresPermission("dict:list")
    public Result<Map<String, List<DictItemVO>>> listItemsBatch(@RequestBody List<String> dictCodes) {
        Map<String, List<DictItemVO>> result = new HashMap<>(dictCodes.size());
        for (String code : dictCodes) {
            result.put(code, dictService.listItemsByCode(code));
        }
        return Result.success(result);
    }

    /**
     * 新增/修改字典类型（运营后台用）
     *
     * @param dictType 字典类型
     * @return 字典类型 ID
     */
    @PostMapping("/type")
    @Operation(summary = "新增/修改字典类型", description = "id 为空走新增，非空走修改，触发缓存清理")
    @RequiresPermission("dict:type:add")
    public Result<Long> saveOrUpdateType(@RequestBody DictType dictType) {
        Long id = dictService.saveOrUpdateType(dictType);
        return Result.success(id);
    }

    /**
     * 新增/修改字典项（运营后台用）
     *
     * @param dictData 字典项
     * @return 字典项 ID
     */
    @PostMapping("/data")
    @Operation(summary = "新增/修改字典项", description = "id 为空走新增，非空走修改，触发缓存清理")
    @RequiresPermission("dict:data:add")
    public Result<Long> saveOrUpdateData(@RequestBody DictData dictData) {
        Long id = dictService.saveOrUpdateData(dictData);
        return Result.success(id);
    }

}
