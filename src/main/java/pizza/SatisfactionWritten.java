
package pizza;

public class SatisfactionWritten extends AbstractEvent {

    private Long id;
    private Long customerId;
    private String menuOption;
    private String satisfactionComment;
    private Integer satisfactionLevel;
    private Integer isSatisfactionWritten;

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
}
