package com.buu.auth.common;

import lombok.Data;

/**
 * 统一接口返回结果
 *
 * @param <T> 响应数据类型
 */
@Data
public class R<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> R<T> success(T data) {
        R<T> result = new R<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> R<T> fail(Integer code, String message) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMsg(message);
        return result;
    }
}
