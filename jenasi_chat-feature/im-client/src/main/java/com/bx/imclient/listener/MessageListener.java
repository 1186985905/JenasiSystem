package com.bx.imclient.listener;


import com.bx.imcommon.model.IMSendResult;

import java.util.List;

//该接口用于处理消息，process方法用于处理消息发送的结果
public interface MessageListener<T> {

     void process(List<IMSendResult<T>> result);

}
