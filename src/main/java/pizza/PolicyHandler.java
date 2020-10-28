package pizza;

import org.springframework.beans.BeanUtils;
import pizza.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
public class PolicyHandler{
    @Autowired SatisfactionRepository satisfactionRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCompleted_SatisfactionRequest(@Payload DeliveryCompleted deliveryCompleted){

        if(deliveryCompleted.isMe()){
            System.out.println("##### listener SatisfactionRequest : " + deliveryCompleted.toJson());
            Satisfaction satisfaction = new Satisfaction();
            BeanUtils.copyProperties(deliveryCompleted, satisfaction);
            satisfaction.setId(deliveryCompleted.getId());
            satisfaction.setIsSatisfactionWritten(0);
            satisfactionRepository.save(satisfaction);
            System.out.println("##### satisfaction policy activated (data saved) by PolicyHandler");
            System.out.println(MessageFormat.format("###### /{0}/{1}/{2}/{3}/"
                    , satisfaction.getId(), satisfaction.getCustomerId(), satisfaction.getIsSatisfactionWritten(), deliveryCompleted.getTimestamp()));
        }
    }

}
