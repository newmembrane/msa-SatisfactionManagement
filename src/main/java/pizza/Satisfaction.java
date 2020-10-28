package pizza;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

import java.text.MessageFormat;

@Entity
@Table(name="Satisfaction_table")
public class Satisfaction {

    @Id
    private Long id;
    private Long customerId;
    private String menuOption;
    private String satisfactionComment;
    private Integer satisfactionLevel;
    private Integer isSatisfactionWritten;
    private String address;

    @PostUpdate
    @PostPersist
    public void onPostPersist(){

        if(1 == getIsSatisfactionWritten()) {
            System.out.println(MessageFormat.format("@#$ Called by Satisfaction on PostPersist /id:{0}/{1}/{2}/", getId(), getCustomerId(), getIsSatisfactionWritten()));
            SatisfactionWritten satisfactionWritten = new SatisfactionWritten();
            BeanUtils.copyProperties(this, satisfactionWritten);
            satisfactionWritten.publishAfterCommit();
            System.out.println("@#$ KAFKA publish completed");
            //Following code causes dependency to pizza.external APIs
            // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

            pizza.external.PaymentHistory paymentHistory = new pizza.external.PaymentHistory();
            // mappings goes here
            SatisfactionManagementApplication.applicationContext.getBean(pizza.external.PaymentHistoryService.class)
                    .payment(paymentHistory);
            System.out.println("@#$ external API request completed");
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public String getMenuOption() {
        return menuOption;
    }

    public void setMenuOption(String menuOption) {
        this.menuOption = menuOption;
    }
    public String getSatisfactionComment() {
        return satisfactionComment;
    }

    public void setSatisfactionComment(String satisfactionComment) {
        this.satisfactionComment = satisfactionComment;
    }
    public Integer getSatisfactionLevel() {
        return satisfactionLevel;
    }

    public void setSatisfactionLevel(Integer satisfactionLevel) {
        this.satisfactionLevel = satisfactionLevel;
    }
    public Integer getIsSatisfactionWritten() {
        return isSatisfactionWritten;
    }

    public void setIsSatisfactionWritten(Integer isSatisfactionWritten) {
        this.isSatisfactionWritten = isSatisfactionWritten;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
