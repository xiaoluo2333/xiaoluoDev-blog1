package xl.xldevblog.main.dto;

/**
 * 喵～ 通用 API 响应包装类！
 * 所有接口统一用这个格式返回数据，
 * 前端就不用为每种响应写不同的解析逻辑啦～
 *
 * 使用方式喵：
 * <pre>
 *   成功时：ApiResponse.ok(data)
 *   失败时：ApiResponse.error("出错了喵～")
 * </pre>
 */
public class ApiResponse {

    /** 请求是否成功（true = 成功，false = 失败） */
    private boolean success;

    /** 提示消息，成功或失败时的描述信息 */
    private String message;

    /** 响应数据，可以是任意类型 */
    private Object data;

    /**
     * 无参构造器，用于序列化喵
     */
    public ApiResponse() {}

    /**
     * 全参构造器
     */
    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 喵～ 操作成功！
     * 返回带数据的成功响应
     *
     * @param data 要返回的数据
     * @return 成功响应对象
     */
    public static ApiResponse ok(Object data) {
        return new ApiResponse(true, "操作成功喵～", data);
    }

    /**
     * 喵呜… 操作失败了！
     * 返回带错误消息的失败响应
     *
     * @param message 错误描述
     * @return 失败响应对象
     */
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }

    // ===== Getter / Setter =====

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}