package org.dam.support;

import org.dam.dto.AuthLoginDTO;
import org.dam.dto.RefreshTokenDTO;
import org.dam.entity.DictData;
import org.dam.entity.DictType;
import org.dam.entity.Permission;
import org.dam.entity.Role;
import org.dam.entity.User;
import org.dam.entity.UserAuth;
import org.dam.vo.DictItemVO;
import org.dam.vo.TokenVO;

import static org.dam.support.TestConstants.*;

/**
 * 测试数据构造器（Test Data Builder 模式）
 * 链式构造测试对象，默认值为"合法成功场景"，可通过方法切换到失败场景
 * 用法：TestDataBuilder.userAuth().userId(2L).identifier("zhangsan").build();
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
public final class TestDataBuilder {

    private TestDataBuilder() {
    }

    // ========== UserAuth ==========
    public static UserAuthBuilder userAuth() {
        return new UserAuthBuilder();
    }

    public static final class UserAuthBuilder {
        private Long id = 1L;
        private Long userId = ADMIN_ID;
        private Integer authType = AUTH_TYPE_PASSWORD;
        private String identifier = IDENTIFIER_ADMIN;
        private String credential = HASH_ADMIN;
        private Integer status = 1;

        public UserAuthBuilder id(Long id) { this.id = id; return this; }
        public UserAuthBuilder userId(Long userId) { this.userId = userId; return this; }
        public UserAuthBuilder authTypePassword() { this.authType = AUTH_TYPE_PASSWORD; return this; }
        public UserAuthBuilder identifier(String identifier) { this.identifier = identifier; return this; }
        public UserAuthBuilder credential(String credential) { this.credential = credential; return this; }
        public UserAuthBuilder validHash() { this.credential = HASH_ADMIN; return this; }
        public UserAuthBuilder status(Integer status) { this.status = status; return this; }
        public UserAuthBuilder enabled() { this.status = 1; return this; }
        public UserAuthBuilder disabled() { this.status = 0; return this; }

        public UserAuth build() {
            UserAuth auth = new UserAuth();
            auth.setId(id);
            auth.setUserId(userId);
            auth.setAuthType(authType);
            auth.setIdentifier(identifier);
            auth.setCredential(credential);
            auth.setStatus(status);
            return auth;
        }
    }

    // ========== User ==========
    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static final class UserBuilder {
        private Long id = ADMIN_ID;
        private String username = IDENTIFIER_ADMIN;
        private String nickname = "管理员";
        private String phone = "13800138000";
        private String email = "admin@dam.com";
        private Integer gender = 1;
        private Integer status = 1;

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder nickname(String nickname) { this.nickname = nickname; return this; }
        public UserBuilder phone(String phone) { this.phone = phone; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder status(Integer status) { this.status = status; return this; }
        public UserBuilder enabled() { this.status = 1; return this; }
        public UserBuilder disabled() { this.status = 0; return this; }
        public UserBuilder locked() { this.status = 2; return this; }
        public UserBuilder pending() { this.status = 3; return this; }

        public User build() {
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setNickname(nickname);
            user.setPhone(phone);
            user.setEmail(email);
            user.setGender(gender);
            user.setStatus(status);
            return user;
        }
    }

    // ========== AuthLoginDTO ==========
    public static LoginDTOBuilder loginDTO() {
        return new LoginDTOBuilder();
    }

    public static final class LoginDTOBuilder {
        private String identifier = IDENTIFIER_ADMIN;
        private String credential = PASSWORD_ADMIN;
        private Integer authType = AUTH_TYPE_PASSWORD;

        public LoginDTOBuilder identifier(String identifier) { this.identifier = identifier; return this; }
        public LoginDTOBuilder credential(String credential) { this.credential = credential; return this; }
        public LoginDTOBuilder authType(Integer authType) { this.authType = authType; return this; }

        public AuthLoginDTO build() {
            AuthLoginDTO dto = new AuthLoginDTO();
            dto.setIdentifier(identifier);
            dto.setCredential(credential);
            dto.setAuthType(authType);
            return dto;
        }
    }

    // ========== RefreshTokenDTO ==========
    public static RefreshTokenDTOBuilder refreshTokenDTO() {
        return new RefreshTokenDTOBuilder();
    }

    public static final class RefreshTokenDTOBuilder {
        private String refreshToken = "fake-refresh-token";

        public RefreshTokenDTOBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public RefreshTokenDTO build() {
            RefreshTokenDTO dto = new RefreshTokenDTO();
            dto.setRefreshToken(refreshToken);
            return dto;
        }
    }

    // ========== TokenVO ==========
    public static TokenVOBuilder tokenVO() {
        return new TokenVOBuilder();
    }

    public static final class TokenVOBuilder {
        private String accessToken = "access-token";
        private String refreshToken = "refresh-token";
        private String tokenType = "Bearer";
        private Long expiresIn = ACCESS_TOKEN_EXPIRE_MINUTES * 60L;
        private Long userId = ADMIN_ID;
        private String username = IDENTIFIER_ADMIN;

        public TokenVOBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public TokenVOBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public TokenVOBuilder userId(Long userId) { this.userId = userId; return this; }
        public TokenVOBuilder username(String username) { this.username = username; return this; }

        public TokenVO build() {
            TokenVO vo = new TokenVO();
            vo.setAccessToken(accessToken);
            vo.setRefreshToken(refreshToken);
            vo.setTokenType(tokenType);
            vo.setExpiresIn(expiresIn);
            vo.setUserId(userId);
            vo.setUsername(username);
            return vo;
        }
    }

    // ========== Role ==========
    public static RoleBuilder role() {
        return new RoleBuilder();
    }

    public static final class RoleBuilder {
        private Long id = 1L;
        private String roleCode = ROLE_ADMIN;
        private String roleName = "超级管理员";
        private Integer status = 1;
        private Integer builtIn = 0;

        public RoleBuilder id(Long id) { this.id = id; return this; }
        public RoleBuilder code(String code) { this.roleCode = code; return this; }
        public RoleBuilder name(String name) { this.roleName = name; return this; }
        public RoleBuilder builtIn() { this.builtIn = 1; return this; }
        public RoleBuilder notBuiltIn() { this.builtIn = 0; return this; }

        public Role build() {
            Role role = new Role();
            role.setId(id);
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setStatus(status);
            role.setBuiltIn(builtIn);
            return role;
        }
    }

    // ========== Permission ==========
    public static PermissionBuilder permission() {
        return new PermissionBuilder();
    }

    public static final class PermissionBuilder {
        private Long id = 1L;
        private String permissionCode = PERM_USER_LIST;
        private String permissionName = "用户查询";
        private Integer type = 3;
        private Long parentId = 0L;
        private Integer status = 1;

        public PermissionBuilder id(Long id) { this.id = id; return this; }
        public PermissionBuilder code(String code) { this.permissionCode = code; return this; }
        public PermissionBuilder name(String name) { this.permissionName = name; return this; }

        public Permission build() {
            Permission permission = new Permission();
            permission.setId(id);
            permission.setPermissionCode(permissionCode);
            permission.setPermissionName(permissionName);
            permission.setType(type);
            permission.setParentId(parentId);
            permission.setStatus(status);
            return permission;
        }
    }

    // ========== DictType ==========
    public static DictTypeBuilder dictType() {
        return new DictTypeBuilder();
    }

    public static final class DictTypeBuilder {
        private Long id = 1L;
        private String dictCode = "user_status";
        private String dictNameZh = "用户状态";
        private String dictNameEn = "User Status";
        private Integer status = 1;

        public DictTypeBuilder id(Long id) { this.id = id; return this; }
        public DictTypeBuilder code(String code) { this.dictCode = code; return this; }
        public DictTypeBuilder nameZh(String nameZh) { this.dictNameZh = nameZh; return this; }
        public DictTypeBuilder nameEn(String nameEn) { this.dictNameEn = nameEn; return this; }

        public DictType build() {
            DictType type = new DictType();
            type.setId(id);
            type.setDictCode(dictCode);
            type.setDictNameZh(dictNameZh);
            type.setDictNameEn(dictNameEn);
            type.setStatus(status);
            return type;
        }
    }

    // ========== DictData ==========
    public static DictDataBuilder dictData() {
        return new DictDataBuilder();
    }

    public static final class DictDataBuilder {
        private Long id = 1L;
        private Long dictTypeId = 1L;
        private String dictLabelZh = "禁用";
        private String dictLabelEn = "Disabled";
        private String dictValue = "0";
        private String cssClass = "danger";
        private Integer sort = 1;
        private Integer status = 1;

        public DictDataBuilder id(Long id) { this.id = id; return this; }
        public DictDataBuilder typeId(Long typeId) { this.dictTypeId = typeId; return this; }
        public DictDataBuilder labelZh(String labelZh) { this.dictLabelZh = labelZh; return this; }
        public DictDataBuilder labelEn(String labelEn) { this.dictLabelEn = labelEn; return this; }
        public DictDataBuilder value(String value) { this.dictValue = value; return this; }
        public DictDataBuilder cssClass(String cssClass) { this.cssClass = cssClass; return this; }
        public DictDataBuilder sort(Integer sort) { this.sort = sort; return this; }
        public DictDataBuilder disabled() { this.status = 0; return this; }

        public DictData build() {
            DictData data = new DictData();
            data.setId(id);
            data.setDictTypeId(dictTypeId);
            data.setDictLabelZh(dictLabelZh);
            data.setDictLabelEn(dictLabelEn);
            data.setDictValue(dictValue);
            data.setCssClass(cssClass);
            data.setSort(sort);
            data.setStatus(status);
            return data;
        }
    }

    // ========== DictItemVO ==========
    public static DictItemVOBuilder dictItemVO() {
        return new DictItemVOBuilder();
    }

    public static final class DictItemVOBuilder {
        private String value = "0";
        private String label = "禁用";
        private String cssClass = "danger";
        private Integer sort = 1;

        public DictItemVOBuilder value(String value) { this.value = value; return this; }
        public DictItemVOBuilder label(String label) { this.label = label; return this; }
        public DictItemVOBuilder cssClass(String cssClass) { this.cssClass = cssClass; return this; }
        public DictItemVOBuilder sort(Integer sort) { this.sort = sort; return this; }

        public DictItemVO build() {
            DictItemVO vo = new DictItemVO();
            vo.setValue(value);
            vo.setLabel(label);
            vo.setCssClass(cssClass);
            vo.setSort(sort);
            return vo;
        }
    }
}
