package com.jsj.datacenter.adapter.advices;

import com.alibaba.fastjson2.JSONObject;
import com.jsj.datacenter.adapter.annotation.EasyResponse;
import com.jsj.datacenter.infrastructure.common.entity.AjaxResult;
import com.jsj.datacenter.infrastructure.common.entity.TableDataInfo;
import com.jsj.datacenter.infrastructure.common.exceptions.ServiceException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {
    /**
     * @param returnType
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 类或方法是否有EasyResponse注解
        if (null == returnType.getMethod()) {
            return false;
        }
        return returnType.getMethod().isAnnotationPresent(EasyResponse.class) || returnType.getContainingClass().isAnnotationPresent(EasyResponse.class);
    }

    /**
     * @param body
     * @param returnType
     * @param selectedContentType
     * @param selectedConverterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof AjaxResult) {
            return body;
        }
        if (body instanceof TableDataInfo) {
            JSONObject json = JSONObject.from(body);
            json.remove("code");
            json.remove("msg");
            return AjaxResult.success(json);
        }
        return AjaxResult.success(body);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<AjaxResult> handleException(ServiceException ex) {
        return new ResponseEntity<>(AjaxResult.error(ex.getCode(), ex.getMessage()), HttpStatus.OK);
    }

}
