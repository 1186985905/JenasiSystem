package com.bx.imserver.netty.processor;

import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.contant.IMRedisKey;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.IMRecvInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imcommon.model.IMSendResult;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.imcommon.mq.RedisMQTemplate;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMessageProcessor extends AbstractMessageProcessor<IMRecvInfo> {

    private final RedisMQTemplate redisMQTemplate;

    @Override
    public void process(IMRecvInfo recvInfo) {
        log.info("接收到系统消息，开始流式推送...");
        String data = recvInfo.getData().toString();
        int chunkSize = 50; // 每块字符数（按需调整）
        List<String> chunks = splitIntoChunks(data, chunkSize);

        for (IMUserInfo receiver : recvInfo.getReceivers()) {
            ChannelHandlerContext channelCtx = UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
            if (channelCtx == null) {
                sendResult(recvInfo, IMSendCode.NOT_FIND_CHANNEL);
                continue;
            }

            try {
                // 发送开始标记
                sendStreamStart(channelCtx);

                // 逐块发送
                for (String chunk : chunks) {
                    IMSendInfo<String> chunkInfo = new IMSendInfo<>();
                    chunkInfo.setCmd(IMCmdType.STREAM_MESSAGE.code());
                    chunkInfo.setData(chunk);
                    channelCtx.write(chunkInfo); // 不立即flush
                    // 模拟延迟（按需调整）
                    Thread.sleep(50);
                }
                channelCtx.flush(); // 最终一次性flush
                sendResult(recvInfo, IMSendCode.SUCCESS);

                // 发送结束标记
                sendStreamEnd(channelCtx);
            } catch (Exception e) {
                sendResult(recvInfo, IMSendCode.UNKONW_ERROR);
                log.error("流式消息发送异常", e);
            }
        }

//        log.info("接收到系统消息,接收用户数量:{}，内容:{}",  recvInfo.getReceivers().size(), recvInfo.getData());
//        for (IMUserInfo receiver : recvInfo.getReceivers()) {
//            try {
//                ChannelHandlerContext channelCtx =
//                    UserChannelCtxMap.getChannelCtx(receiver.getId(), receiver.getTerminal());
//                if (!Objects.isNull(channelCtx)) {
//                    // 推送消息到用户
//                    IMSendInfo<Object> sendInfo = new IMSendInfo<>();
//                    sendInfo.setCmd(IMCmdType.SYSTEM_MESSAGE.code());
//                    sendInfo.setData(recvInfo.getData());
//                    channelCtx.channel().writeAndFlush(sendInfo);
//                    // 消息发送成功确认
//                    sendResult(recvInfo, IMSendCode.SUCCESS);
//                } else {
//                    // 消息推送失败确认
//                    sendResult(recvInfo, IMSendCode.NOT_FIND_CHANNEL);
//                    log.error("未找到channel，接收者:{}，内容:{}", receiver.getId(), recvInfo.getData());
//                }
//            } catch (Exception e) {
//                // 消息推送失败确认
//                sendResult(recvInfo, IMSendCode.UNKONW_ERROR);
//                log.error("发送异常，,接收者:{}，内容:{}", receiver.getId(), recvInfo.getData(), e);
//            }
//        }
    }

    private List<String> splitIntoChunks(String data, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < data.length(); i += chunkSize) {
            chunks.add(data.substring(i, Math.min(i + chunkSize, data.length())));
        }
        return chunks;
    }

    private void sendStreamStart(ChannelHandlerContext ctx) {
        IMSendInfo<String> startMarker = new IMSendInfo<>();
        startMarker.setCmd(IMCmdType.STREAM_MESSAGE.code());
        startMarker.setData("[START_STREAM]");
        ctx.writeAndFlush(startMarker);
    }

    private void sendStreamEnd(ChannelHandlerContext ctx) {
        IMSendInfo<String> endMarker = new IMSendInfo<>();
        endMarker.setCmd(IMCmdType.STREAM_MESSAGE.code());
        endMarker.setData("[END_STREAM]");
        ctx.writeAndFlush(endMarker);
    }

    private void sendResult(IMRecvInfo recvInfo, IMSendCode sendCode) {
        if (recvInfo.getSendResult()) {
            IMSendResult<Object> result = new IMSendResult<>();
            result.setReceiver(recvInfo.getReceivers().get(0));
            result.setCode(sendCode.code());
            result.setData(recvInfo.getData());
            // 推送到结果队列
            String key = StrUtil.join(":",IMRedisKey.IM_RESULT_SYSTEM_QUEUE,recvInfo.getServiceName());
            redisMQTemplate.opsForList().rightPush(key, result);
        }
    }
}
