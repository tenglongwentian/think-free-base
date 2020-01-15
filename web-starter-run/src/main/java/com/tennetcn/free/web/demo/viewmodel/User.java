package com.tennetcn.free.web.demo.viewmodel;

import com.tennetcn.free.core.validator.annotation.AtLeastOneNotEmpty;
import com.tennetcn.free.core.validator.annotation.MethodCheck;
import com.tennetcn.free.web.demo.apis.CheckTest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author chfree
 * @email chfree001@gmail.com
 * @create 2020-01-15 12:09
 * @comment
 */

@Data
@MethodCheck(clazz = CheckTest.class,method = "checkTest",parameterTypes = {User.class})
@AtLeastOneNotEmpty(fields={"userId","userName"})
public class User {
    private String userId;

    private String userName;

    @NotBlank
    private String password;
}
