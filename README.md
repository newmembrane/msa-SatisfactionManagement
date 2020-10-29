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


### 기능적/비기능적 요구사항을 만족하는지 검증
1. 기능적 요구사항 
    1. 고객이 메뉴를 주문한다 (OK)
    1. 고객이 결제한다 (OK)
    1. 주문이 되면 주문/배송 시스템에 전달된다. (OK)
    1. 판매자가 확인하여 제작을 시작한다. (OK)
    1. 배송자가 확인하여 배송을 시작한다. (OK)
    1. 배송자가 배송 완료 후 완료를 입력한다. (OK)
    1. 주문이 취소되면 결제가 취소된다 (OK)
    1. 주문 상태가 변경될 때마다 일림 관리자에 기록한다. (OK)

1. 비기능적 요구사항
    1. 결제가 되지 않은 주문건은 아예 거래가 성립되지 않아야 한다  Sync 호출  (OK)
    1. 주문/배송 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency (OK)
    1. 만족도 작성과 조회를 분리하여 시스템 성능을 향상시킨다.  (CQRS) (OK)


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

![폴리글랏01](https://user-images.githubusercontent.com/44703764/97535830-97426d80-19ff-11eb-8cb3-04bfdbe683ce.png)
![mongodb연동확인](https://user-images.githubusercontent.com/44703764/97535857-a1646c00-19ff-11eb-93a6-4ff3c76564c0.png)
데이터 확인하기

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
![auto scale  늘어나기 전 pods list](https://user-images.githubusercontent.com/44703764/97434966-22215a80-1963-11eb-8bfe-a32d7f9cfcf4.png)

- 무한 워크로드를 걸어준 뒤 cpu 사용량을 모니터링
```
while true;do curl 52.149.189.108:8080/orderDeliveries;done
```
```
watch -n 1 kubectl get hpa
```
![auto scale  fullload - replica 10으로 늘어남](https://user-images.githubusercontent.com/44703764/97434956-1df53d00-1963-11eb-9e39-2cf8aa5f49e3.png)

- cpu 사용량이 20%를 넘어가면서 스케일 아웃이 벌어지는 것을 확인할 수 있다:
![auto scale  load target 초괴하여 replica 늘어남](https://user-images.githubusercontent.com/44703764/97434963-1f266a00-1963-11eb-9081-8fb8bcd7ae48.png)

- ![auto scale  fullload - replica 10으로 늘어난 pods list](https://user-images.githubusercontent.com/44703764/97434995-2d748600-1963-11eb-81ac-8afeef211f08.png)

- 무한 로드 중단 후 cpu 사용량이 안정되면 replica가 줄어드는 것을 확인할 수 있다:
![auto scale  부하정상화된 후 1로 줄어듦](https://user-images.githubusercontent.com/44703764/97434949-1c2b7980-1963-11eb-9e2a-56509717cf6a.png)

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









# 평가항목
## Saga
orderCanceled에서 paymentCancel로 pub 후 PaymentHistory 변경

## CQRS
![image](https://user-images.githubusercontent.com/34112237/97382926-c1b4fd80-190f-11eb-9ccd-1d12a7729e1d.png)
- view 스티커 구현 (삭제)
- 핵심 Biz로직은 동기식 처리 (Req/Res) 및 비동기식 처리(pub/sub)
- 조회 목적 이력 관리를 위하여 비동기(pub/sub) 방식으로 주요 Event 별도 로그 저장
(그림. 모델에서 각 event 표시, 저장 로직 표시)

## Correlation
- Correlation key saga cqrs 자동 득점

## Req/Resp
- 

## Gateway

## Deploy/Pipeline
PizzaOrderManagement GITHUB에 신규 파일 추가
![image](https://user-images.githubusercontent.com/34112237/97384017-2ec99280-1912-11eb-8f36-26b3c444b234.png)

CI/CD 파이프라인 자동 적용
![image](https://user-images.githubusercontent.com/34112237/97383819-d1354600-1911-11eb-9c0a-912216b45410.png)
![image](https://user-images.githubusercontent.com/34112237/97384616-61c05600-1913-11eb-89ba-813220216e9e.png)

```
skcc02@Azure:~$ kubectl get all
NAME                                           READY   STATUS    RESTARTS   AGE
pod/gateway-6f676b79b9-zpfrn                   1/1     Running   0          36m
pod/httpie                                     1/1     Running   1          17h
pod/notificationmanagement-6776554c78-ltj4d    1/1     Running   0          23m
pod/orderdeliverymanagement-7546b6f744-vvh9n   1/1     Running   0          15h
pod/paymentmanagement-8656994556-6qw66         1/1     Running   0          101s
pod/pizzaordermanagement-667cb666bd-d5fln      1/1     Running   0          24m

NAME                              TYPE           CLUSTER-IP     EXTERNAL-IP      PORT(S)          AGE
service/gateway                   LoadBalancer   10.0.94.200    52.149.189.108   8080:30155/TCP   36m
service/kubernetes                ClusterIP      10.0.0.1       <none>           443/TCP          16h
service/notificationmanagement    ClusterIP      10.0.131.123   <none>           8080/TCP         125m
service/orderdeliverymanagement   ClusterIP      10.0.208.63    <none>           8080/TCP         16h
service/paymentmanagement         ClusterIP      10.0.57.39     <none>           8080/TCP         3m59s
service/pizzaordermanagement      ClusterIP      10.0.244.64    <none>           8080/TCP         3h34m

NAME                                      READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/gateway                   1/1     1            1           36m
deployment.apps/notificationmanagement    1/1     1            1           125m
deployment.apps/orderdeliverymanagement   1/1     1            1           16h
deployment.apps/paymentmanagement         1/1     1            1           4m2s
deployment.apps/pizzaordermanagement      1/1     1            1           3h34m

NAME                                                 DESIRED   CURRENT   READY   AGE
replicaset.apps/gateway-6f676b79b9                   1         1         1       36m
replicaset.apps/notificationmanagement-6776554c78    1         1         1       24m
replicaset.apps/notificationmanagement-67f8757474    0         0         0       125m
replicaset.apps/notificationmanagement-6b7b6d796b    0         0         0       124m
replicaset.apps/orderdeliverymanagement-54fcf466cf   0         0         0       16h
replicaset.apps/orderdeliverymanagement-5cf5dc657c   0         0         0       16h
replicaset.apps/orderdeliverymanagement-65b5f4fcfb   0         0         0       16h
replicaset.apps/orderdeliverymanagement-7546b6f744   1         1         1       15h
replicaset.apps/orderdeliverymanagement-7ff58767b9   0         0         0       16h
replicaset.apps/orderdeliverymanagement-cf84dcfc     0         0         0       16h
replicaset.apps/paymentmanagement-559b8dc87b         0         0         0       3m59s
replicaset.apps/paymentmanagement-5969d57847         0         0         0       4m2s
replicaset.apps/paymentmanagement-8656994556         1         1         1       102s
replicaset.apps/pizzaordermanagement-57c4d9cc7b      0         0         0       70m
replicaset.apps/pizzaordermanagement-667cb666bd      1         1         1       24m
replicaset.apps/pizzaordermanagement-66d968895c      0         0         0       66m
replicaset.apps/pizzaordermanagement-7779c544df      0         0         0       3h34m
replicaset.apps/pizzaordermanagement-7d54c94bb6      0         0         0       62m
replicaset.apps/pizzaordermanagement-85c5f67d85      0         0         0       3h34m
```


## Circuit Breaker 




## Polyglot
   - 랭기지 레벨 또는 데이터베이스 레벨


   
   
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
