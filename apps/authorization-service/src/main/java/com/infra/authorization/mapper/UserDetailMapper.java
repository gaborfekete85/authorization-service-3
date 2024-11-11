package com.infra.authorization.mapper;

import com.infra.authorization.controller.dto.SignInRequest;
import com.infra.authorization.controller.dto.SignUpRequest;
import com.infra.authorization.mapper.config.CentralConfig;
import com.infra.authorization.persistence.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralConfig.class)
public interface UserDetailMapper {
    @Mapping(target = "name", expression = "java(java.lang.String.format(\"%s %s\", signUpRequest.getFirstName(), signUpRequest.getLastName()))")
    @Mapping(target = "provider", expression = "java(com.infra.authorization.model.AuthProvider.local)")
    @Mapping(target = "emailVerificationCode", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "emailVerified",  constant = "false")
    @Mapping(target = "registrationDateTime",  expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "password", expression = "java(signUpRequest.getPassword())")
    @Mapping(target = "roles",  ignore = true)
    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "imageUrl",  ignore = true)
    @Mapping(target = "providerId",  ignore = true)
    User mapFromSignUpRequestToDomain(SignUpRequest signUpRequest);

    @Mapping(target = "emailVerified",  constant = "false")
    @Mapping(target = "roles",  ignore = true)
    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "imageUrl",  ignore = true)
    @Mapping(target = "providerId",  ignore = true)
    @Mapping(target = "name",  ignore = true)
    @Mapping(target = "emailVerificationCode",  ignore = true)
    @Mapping(target = "provider",  ignore = true)
    @Mapping(target = "phone",  ignore = true)
    @Mapping(target = "firstName",  ignore = true)
    @Mapping(target = "lastName",  ignore = true)
    @Mapping(target = "registrationDateTime",  ignore = true)
    User mapFromSignInRequestToDomain(SignInRequest signInRequest);
}
