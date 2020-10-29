![image](https://user-images.githubusercontent.com/34112237/97147961-ae8a1c80-17ad-11eb-80da-93f2f07108d4.png)
# pizza 

# 서비스 시나리오

## 기능적 요구사항
1. 고객이 메뉴를 주문한다
1. 고객이 결제한다
1. 주문이 되면 주문/배송 시스템에 전달된다.
1. 판매자가 확인하여 제작을 시작한다.
1. 배송자가 확인하여 배송을 시작한다.
1. 배송자가 배송 완료 후 완료를 입력한다.
1. 주문이 취소되면 결제가 취소된다
1. 주문 상태가 변경될 때마다 일림 관리자에 기록한다.

## 비기능적 요구사항
1. 트랜잭션
    1. 결제가 되지 않은 주문건은 아예 거래가 성립되지 않아야 한다  Sync 호출 
1. 장애격리
    1. 주문/배송 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
1. 성능
    1. 주문과 조회를 분리하여 시스템 성능을 향상시킨다.  (CQRS)


# 분석 설계

## Event Storming 결과
* MESEz로 모델링한 이벤트스토밍 결과 : http://www.msaez.io/#/storming/9jZsKaOObZg9sIWkpGQ0AqEx6kv2/mine/43513577ef0b64659209b3c97904ee99/-MK-BBvebd33BLu8-JwH

### 개인과제최종
![개인 이벤트 도출](https://user-images.githubusercontent.com/44703764/97536753-fc4a9300-1a00-11eb-8bbb-6bd9ae436795.png)
* satisfactionView 구현
* 조회 목적 이력 관리를 위하여 비동기(pub/sub) 방식으로 주요 Event 별도 로그 저장

## 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/34112237/97233785-dcf60f00-1822-11eb-9583-abc3d6ae706e.png)
![헥사고날](https://user-images.githubusercontent.com/44703764/97535821-94477d00-19ff-11eb-9efc-23b668385a05.png)

# 구현
## 실행 결과
![배달완료 후 satisfactions_1 생성됨](https://user-images.githubusercontent.com/44703764/97535880-acb79780-19ff-11eb-86f8-ac22cbbeceae.png)
![satisfactionWritten으로부터 받은 request 처리 로그](https://user-images.githubusercontent.com/44703764/97535889-b0e3b500-19ff-11eb-99e2-21d77d482861.png)
![satisfactionWritten으로부터 받은 request 처리됐는지 조회](https://user-images.githubusercontent.com/44703764/97535894-b2ad7880-19ff-11eb-84d2-64031cf5d49e.png)
![전체 kafka 메시지](https://user-images.githubusercontent.com/44703764/97535903-b3dea580-19ff-11eb-8559-f53ad17eb017.png)


## 동기식 호출과 Fallback 처리
feign client를 사용하여 동기식 일관성을 유지하는 트랜잭션으로 처리하도록 함.
```
    @PostPersist
    public void onPostPersist(){
        
            ...

            pizza.external.PaymentHistory paymentHistory = new pizza.external.PaymentHistory();
            BeanUtils.copyProperties(this, paymentHistory);
            // mappings goes here
            SatisfactionManagementApplication.applicationContext.getBean(pizza.external.PaymentHistoryService.class)
                    .payment(paymentHistory);
            System.out.println("@#$ external API request completed");
        }
    }
```


## 비동기식 호출 / 시간적 디커플링
publish - SatisfactionWritten 이벤트 연동
```
@PostPersist
    public void onPostPersist(){

        if(1 == getIsSatisfactionWritten()) {
            System.out.println(MessageFormat.format("@#$ Called by Satisfaction on PostPersist /id:{0}/{1}/{2}/", getId(), getCustomerId(), getIsSatisfactionWritten()));
            SatisfactionWritten satisfactionWritten = new SatisfactionWritten();
            BeanUtils.copyProperties(this, satisfactionWritten);
            satisfactionWritten.publishAfterCommit();
            System.out.println("@#$ KAFKA publish completed");
            
            ...
        }
    }
```
subscribe
```
    @StreamListener(KafkaProcessor.INPUT)
    public void whenSatisfactionWritten_then_CREATE_1 (@Payload SatisfactionWritten satisfactionWritten) {
        try {
            if (satisfactionWritten.isMe()) {
                // view 객체 생성
                System.out.println("@@@@ SatisfactionWritten start");
                SatisfactionView satisfactionView = new SatisfactionView();
                // view 객체에 이벤트의 Value 를 set 함
                BeanUtils.copyProperties(satisfactionWritten, satisfactionView);
                satisfactionView.setOrderId(satisfactionWritten.getId());
                satisfactionView.setIsSatisfactionWritten(1);
                satisfactionView.setEventType(satisfactionWritten.getEventType());
                satisfactionView.setTimekey(satisfactionWritten.getTimestamp());
                
                ...
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
```



## Gateway
![gateway01](https://user-images.githubusercontent.com/44703764/97528124-4e36ed00-19f0-11eb-843e-432b3f39fa59.png)
![gateway02](https://user-images.githubusercontent.com/44703764/97528128-5000b080-19f0-11eb-9f13-8c9f26d95a08.png)
![gateway03](https://user-images.githubusercontent.com/44703764/97535683-59454980-19ff-11eb-8797-f6da2e01e676.png)
![gateway04](https://user-images.githubusercontent.com/44703764/97535689-5ba7a380-19ff-11eb-8079-aac4dd8603b6.png)


## 폴리글랏 퍼시스턴스
데이터 저장소 유형을 복수 채택하여 서비스를 구현함.
1. H2
![H2사용](https://user-images.githubusercontent.com/44703764/97537297-ec7f7e80-1a01-11eb-9de6-b15aeb7ca9b8.png)
2. MongoDB
![몽고추가](https://user-images.githubusercontent.com/44703764/97538366-9ad7f380-1a03-11eb-9f62-f3a42f2d967d.png)
![폴리글랏01](https://user-images.githubusercontent.com/44703764/97535830-97426d80-19ff-11eb-8cb3-04bfdbe683ce.png)
![mongodb연동확인](https://user-images.githubusercontent.com/44703764/97535857-a1646c00-19ff-11eb-93a6-4ff3c76564c0.png)

# 운영

## CI/CD 설정
![CICD01](https://user-images.githubusercontent.com/44703764/97528113-4a0acf80-19f0-11eb-8adb-e6c53a10b68d.png)
![CICD02](https://user-images.githubusercontent.com/44703764/97528118-4bd49300-19f0-11eb-9cc2-e6f34b3b2690.png)

## 동기식 호출 
간략한 설명 작성
```
소스코드 붙여넣기
```

### Autoscale (HPA)
- 주문배송 관리 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 20프로를 넘어서면 replica 를 최대 10개까지 늘려준다:
```
kubectl autoscale deployment orderdeliverymanagement --cpu-percent=20 --min=1 --max=10
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
watch -n 3 kubectl get all
```
![auto scale  01](https://user-images.githubusercontent.com/44703764/97536759-010f4700-1a01-11eb-9279-5f5c79039f13.png)

- 무한 워크로드를 걸어준 뒤 cpu 사용량을 모니터링
```
while true;do curl 52.149.189.108:8080/orderDeliveries;done
```
```
watch -n 1 kubectl get hpa
```

- cpu 사용량이 20%를 넘어가면서 스케일 아웃이 벌어지는 것을 확인할 수 있다:
![auto scale  02](https://user-images.githubusercontent.com/44703764/97536772-040a3780-1a01-11eb-8f4f-d29ffe554b33.png)

- 무한 로드 중단 후 cpu 사용량이 안정되면 replica가 줄어드는 것을 확인할 수 있다:
![auto scale  03](https://user-images.githubusercontent.com/44703764/97536777-066c9180-1a01-11eb-96fa-242777e02787.png)

###### deployment.yaml 설정
```
spec:
  ...
  template:
    metadata:
      labels:
        app: Paymentmanagement
    spec:
      containers:
        - name: Paymentmanagement
          resources:
            limits:
              cpu: 500m
            requests:
              cpu: 200m
```


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 seige를 사용하여 CI/CD 실행 직전에 워크로드를 모니터링 함.
```
siege -c50 -t60S -r10 http://10.0.94.200:8080/pizzaOrders
```

- git commit을 하여 CI/CD 파이프라인 시작
- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
// 무정지01

배포기간중 Availability 가 평소 100%에서 50% 대로 떨어지는 것을 확인.
원인은 쿠버네티스가 새로 올라온 서비스가 실행 준비가 미흡한 서비스임을 고려하지 않고 성급하게 구버전 서비스를 내렸기 때문. 이를 막기위해 Readiness Probe 를 설정함:

- deployment.yaml 을 수정하여 readiness probe 설정한 후 git commit.

- 동일한 시나리오로 재배포 한 후 Availability 확인:
![readiness  설정 후 무중단 배포된 것 확인함](https://user-images.githubusercontent.com/44703764/97436897-0ec3be80-1966-11eb-8a6e-99b02482995a.png)

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


## Self-healing (Liveness Probe)
- 리스타트되도록 증적 캡쳐	

![image](https://user-images.githubusercontent.com/34112237/97389585-047dd200-191e-11eb-8a2d-11639b262d76.png)

![image](https://user-images.githubusercontent.com/34112237/97389810-a4d3f680-191e-11eb-9635-d01352b6aa07.png)


## Config Map
- application.yml
```
api:
  url:
    payment: ${configurl}
```

- deployment.yml
```
apiVersion: v1
kind: ConfigMap
metadata:
  name: apiurl
data:
  url: http://localhost:8082
```

- configmap 설정
```
kubectl get configmaps pizzaconfigmap -o yaml
```

- Application 활용

![image](https://user-images.githubusercontent.com/34112237/97435550-e8048880-1963-11eb-8434-1325a51c2868.png)




# 자주 사용하는 명령어
   
## kafka 사용법
kafka폴더 이동.

1. zookeeper 실행
```
.\bin\windows\zookeeper-server-start.bat ..\..\config\zookeeper.properties
```
2. kafka server 실행
```
.\bin\windows\kafka-server-start.bat ..\..\config\server.properties
```
메세지 확인하기
```
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic pizza --from-beginning
```
