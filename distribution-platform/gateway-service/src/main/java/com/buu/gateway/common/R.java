package com.buu.gateway.common;

/**
 * Unified API response wrapper for gateway endpoints.
 *
 * @param <T> response data type
 */
public class R<T> {

    private Integer code;
    private String msg;
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * Creates a success response with data.
     *
     * @param data response payload
     * @param <T> response data type
     * @return unified success response
     */
    public static <T> R<T> success(T data) {
        R<T> result = new R<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * Creates a failure response.
     *
     * @param message error message
     * @param <T> response data type
     * @return unified failure response
     */
    public static <T> R<T> fail(String message) {
        R<T> result = new R<>();
        result.setCode(500);
        result.setMsg(message);
        return result;
    }
}
