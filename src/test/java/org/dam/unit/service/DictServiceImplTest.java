package org.dam.unit.service;

import org.dam.entity.DictData;
import org.dam.entity.DictType;
import org.dam.mapper.DictDataMapper;
import org.dam.mapper.DictTypeMapper;
import org.dam.service.DictService;
import org.dam.service.impl.DictServiceImpl;
import org.dam.support.TestDataBuilder;
import org.dam.vo.DictItemVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * DictServiceImpl 单元测试
 * 覆盖字典项查询（中英文 Locale 切换）、标签反查、入参校验、新增/修改分支
 * self 字段通过 @Mock DictService 模拟 @Lazy 自注入
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@DisplayName("功能: DictService - 数据字典")
@ExtendWith(MockitoExtension.class)
class DictServiceImplTest {

    @Mock private DictTypeMapper dictTypeMapper;
    @Mock private DictDataMapper dictDataMapper;
    @Mock private DictService self;

    @InjectMocks
    private DictServiceImpl dictService;

    @Nested
    @DisplayName("场景: 查询字典项 listItemsByCode")
    class ListItemsByCode {

        @Test
        @DisplayName("dictCode 为空返回空集合且不查 DB")
        void should_returnEmpty_when_dictCodeBlank() {
            // when
            List<DictItemVO> result = dictService.listItemsByCode("");

            // then
            assertThat(result).isEmpty();
            verify(dictTypeMapper, never()).selectOne(any());
        }

        @Test
        @DisplayName("字典类型不存在返回空集合")
        void should_returnEmpty_when_typeNotExists() {
            // given
            given(dictTypeMapper.selectOne(any())).willReturn(null);

            // when
            List<DictItemVO> result = dictService.listItemsByCode("not_exists");

            // then
            assertThat(result).isEmpty();
            verify(dictDataMapper, never()).selectList(any());
        }

        @Test
        @DisplayName("中文环境用 zh 标签")
        void should_returnZhLabel_when_chineseLocale() {
            try (MockedStatic<LocaleContextHolder> mocked = mockStatic(LocaleContextHolder.class)) {
                mocked.when(LocaleContextHolder::getLocale).thenReturn(Locale.CHINA);
                // given
                DictType type = TestDataBuilder.dictType().build();
                given(dictTypeMapper.selectOne(any())).willReturn(type);
                given(dictDataMapper.selectList(any())).willReturn(Arrays.asList(
                        TestDataBuilder.dictData().value("0").labelZh("禁用").labelEn("Disabled").build(),
                        TestDataBuilder.dictData().id(2L).value("1").labelZh("启用").labelEn("Enabled")
                                .cssClass("success").sort(2).build()
                ));

                // when
                List<DictItemVO> result = dictService.listItemsByCode("user_status");

                // then
                assertThat(result).hasSize(2);
                assertThat(result.get(0).getValue()).isEqualTo("0");
                assertThat(result.get(0).getLabel()).isEqualTo("禁用");
                assertThat(result.get(0).getCssClass()).isEqualTo("danger");
            }
        }

        @Test
        @DisplayName("英文环境用 en 标签")
        void should_returnEnLabel_when_englishLocale() {
            try (MockedStatic<LocaleContextHolder> mocked = mockStatic(LocaleContextHolder.class)) {
                mocked.when(LocaleContextHolder::getLocale).thenReturn(Locale.US);
                // given
                DictType type = TestDataBuilder.dictType().build();
                given(dictTypeMapper.selectOne(any())).willReturn(type);
                given(dictDataMapper.selectList(any())).willReturn(Collections.singletonList(
                        TestDataBuilder.dictData().value("0").labelZh("禁用").labelEn("Disabled").build()
                ));

                // when
                List<DictItemVO> result = dictService.listItemsByCode("user_status");

                // then
                assertThat(result.get(0).getLabel()).isEqualTo("Disabled");
            }
        }

        @Test
        @DisplayName("英文标签为空时回退 zh 标签")
        void should_fallbackToZh_when_enLabelBlank() {
            try (MockedStatic<LocaleContextHolder> mocked = mockStatic(LocaleContextHolder.class)) {
                mocked.when(LocaleContextHolder::getLocale).thenReturn(Locale.US);
                // given
                DictType type = TestDataBuilder.dictType().build();
                given(dictTypeMapper.selectOne(any())).willReturn(type);
                given(dictDataMapper.selectList(any())).willReturn(Collections.singletonList(
                        TestDataBuilder.dictData().value("0").labelZh("禁用").labelEn("").build()
                ));

                // when
                List<DictItemVO> result = dictService.listItemsByCode("user_status");

                // then
                assertThat(result.get(0).getLabel()).isEqualTo("禁用");
            }
        }
    }

    @Nested
    @DisplayName("场景: 反查标签 getLabel")
    class GetLabel {

        @Test
        @DisplayName("dictValue 存在返回对应 label")
        void should_returnLabel_when_valueExists() {
            // given
            given(self.listItemsByCode("user_status")).willReturn(Arrays.asList(
                    TestDataBuilder.dictItemVO().value("0").label("禁用").build(),
                    TestDataBuilder.dictItemVO().value("1").label("启用").cssClass("success").sort(2).build()
            ));

            // when
            String label = dictService.getLabel("user_status", "1");

            // then
            assertThat(label).isEqualTo("启用");
        }

        @Test
        @DisplayName("dictValue 不存在返回原值")
        void should_returnOriginalValue_when_valueNotExists() {
            // given
            given(self.listItemsByCode("user_status")).willReturn(Collections.singletonList(
                    TestDataBuilder.dictItemVO().value("0").label("禁用").build()
            ));

            // when
            String label = dictService.getLabel("user_status", "99");

            // then
            assertThat(label).isEqualTo("99");
        }

        @Test
        @DisplayName("dictCode 为空返回原值")
        void should_returnOriginalValue_when_dictCodeBlank() {
            // when
            String label = dictService.getLabel("", "0");

            // then
            assertThat(label).isEqualTo("0");
            verify(self, never()).listItemsByCode(any());
        }
    }

    @Nested
    @DisplayName("场景: 校验值合法性 isValidValue")
    class IsValidValue {

        @Test
        @DisplayName("合法值返回 true")
        void should_returnTrue_when_valueValid() {
            // given
            given(self.listItemsByCode("user_status")).willReturn(Collections.singletonList(
                    TestDataBuilder.dictItemVO().value("0").label("禁用").build()
            ));

            // when
            boolean valid = dictService.isValidValue("user_status", "0");

            // then
            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("非法值返回 false")
        void should_returnFalse_when_valueInvalid() {
            // given
            given(self.listItemsByCode("user_status")).willReturn(Collections.singletonList(
                    TestDataBuilder.dictItemVO().value("0").label("禁用").build()
            ));

            // when
            boolean valid = dictService.isValidValue("user_status", "99");

            // then
            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("空值返回 false")
        void should_returnFalse_when_valueBlank() {
            // when
            boolean valid = dictService.isValidValue("user_status", "");

            // then
            assertThat(valid).isFalse();
            verify(self, never()).listItemsByCode(any());
        }
    }

    @Nested
    @DisplayName("场景: 新增/修改字典类型 saveOrUpdateType")
    class SaveOrUpdateType {

        @Test
        @DisplayName("id 为空走 insert")
        void should_insert_when_idNull() {
            // given
            DictType type = TestDataBuilder.dictType().id(null).build();

            // when
            Long id = dictService.saveOrUpdateType(type);

            // then
            verify(dictTypeMapper, times(1)).insert(any(DictType.class));
            verify(dictTypeMapper, never()).updateById(any());
            assertThat(id).isNull();
        }

        @Test
        @DisplayName("id 非空走 update")
        void should_update_when_idNotNull() {
            // given
            DictType type = TestDataBuilder.dictType().id(1L).build();

            // when
            Long id = dictService.saveOrUpdateType(type);

            // then
            verify(dictTypeMapper, times(1)).updateById(any(DictType.class));
            verify(dictTypeMapper, never()).insert(any());
            assertThat(id).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("场景: 新增/修改字典项 saveOrUpdateData")
    class SaveOrUpdateData {

        @Test
        @DisplayName("id 为空走 insert")
        void should_insert_when_idNull() {
            // given
            DictData data = TestDataBuilder.dictData().id(null).build();

            // when
            Long id = dictService.saveOrUpdateData(data);

            // then
            verify(dictDataMapper, times(1)).insert(any(DictData.class));
            verify(dictDataMapper, never()).updateById(any());
            assertThat(id).isNull();
        }

        @Test
        @DisplayName("id 非空走 update")
        void should_update_when_idNotNull() {
            // given
            DictData data = TestDataBuilder.dictData().id(1L).build();

            // when
            Long id = dictService.saveOrUpdateData(data);

            // then
            verify(dictDataMapper, times(1)).updateById(any(DictData.class));
            verify(dictDataMapper, never()).insert(any());
            assertThat(id).isEqualTo(1L);
        }
    }
}
