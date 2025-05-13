package com.bx.implatform.AI;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.implatform.dto.PrivateMessageDTO;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.service.impl.PrivateMessageServiceImpl;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.util.AIMsg;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Start {

    long AIId = 1L; // 机器人用户 ID

    @Resource
    PrivateMessageServiceImpl privateMessageService;   // 私聊消息服务


    //------------------------   创建机器

    // 定时任务调用时
    @Scheduled(fixedRate = 1000)
    public void scheduleAutoReply() {
//
//        Integer terminal = IMTerminalType.WEB.code(); // 假设机器人客服使用WEB终端
//        UserSession session = createVirtualSession(AIId, terminal);
//        SessionContext.setSession(session); // 设置虚拟会话
        try {
            autoReply();
        } finally {
//            SessionContext.removeSession(); // 清除虚拟会话
        }
    }


    // 创建虚拟会话  包含 id 以及 设备
//    private UserSession createVirtualSession(Long userId, Integer terminal) {
//        UserSession session = new UserSession();
//        session.setUserId(userId);
//        session.setTerminal(terminal);
//        return session;
//    }



    // 未读消息 并 自动回复
    public void autoReply() {
        List<PrivateMessage> unreadMessages = findUnreadMessages(AIId);  // 获取AI未读消息列表
        if (unreadMessages.isEmpty()) {
            return;
        }

        for (PrivateMessage message : unreadMessages) {
            String userMessage = message.getContent();  // 获取用户发送消息

            privateMessageService.readedMessage(message.getSendId());  // 将未读消息状态设置为已读

            String botResponse = AIMsg.getAIMsg(userMessage);   //获取ai回复

            PrivateMessageDTO dto = new PrivateMessageDTO();  // 创建一个 私聊 消息 对象
            dto.setRecvId(message.getSendId());  // 设置接收者ID
            dto.setContent(botResponse);
            dto.setType(0);

            // 调用sendMessage方法时，传递机器人客服的用户ID和终端类型
            privateMessageService.sendMessage(dto, AIId, IMTerminalType.WEB.code());  // 接收者 发送者 发送设备

            message.setStatus(MessageStatus.READED.code());  // 设置消息状态为已读
            privateMessageService.updateById(message);  // 更新消息状态

        }
    }


    // 获取未读消息
    private List<PrivateMessage> findUnreadMessages(Long userId) {
        // 查询未读消息
        QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(PrivateMessage::getRecvId, userId)
                .eq(PrivateMessage::getStatus, MessageStatus.UNREAD.code());
        return privateMessageService.list(queryWrapper);
    }
}
