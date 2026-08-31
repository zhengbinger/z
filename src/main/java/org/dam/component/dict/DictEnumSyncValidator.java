package org.dam.component.dict;

import lombok.extern.slf4j.Slf4j;
import org.dam.component.status.UserStatus;
import org.dam.service.DictService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 启动时校验枚举与字典一致性
 * 防止枚举新增值后忘记同步字典，导致枚举与字典双份存储不一致
 * 不一致直接抛 IllegalStateException 阻止启动，强制开发者同步
 *
 * @author zhengbing
 * @since 2026-08-31
 **/
@Slf4j
@Component
@Order(20)
public class DictEnumSyncValidator implements ApplicationRunner {

    /**
     * UserStatus 枚举对应的字典编码
     */
    private static final String USER_STATUS_DICT_CODE = "user_status";

    @Resource
    private DictService dictService;

    @Override
    public void run(ApplicationArguments args) {
        checkUserStatus();
        log.info("枚举与字典一致性校验通过");
    }

    /**
     * 校验 UserStatus 枚举每个值都在 user_status 字典里存在
     */
    private void checkUserStatus() {
        for (UserStatus status : UserStatus.values()) {
            boolean valid = dictService.isValidValue(
                    USER_STATUS_DICT_CODE, String.valueOf(status.getCode()));
            if (!valid) {
                throw new IllegalStateException(
                        "枚举与字典不一致: UserStatus=" + status.name()
                                + ", code=" + status.getCode()
                                + ", dictCode=" + USER_STATUS_DICT_CODE
                                + "；请同步 sys_dict_data 表后重启");
            }
        }
    }

}
