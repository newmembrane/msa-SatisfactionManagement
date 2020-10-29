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

### 01. 이벤트 도출
![01 이벤트 도출](https://user-images.githubusercontent.com/34112237/97229725-22fba480-181c-11eb-830e-c88bf57fd70d.PNG)

### 02. 액터 커맨드 추가하기
![02 액터 커맨드 추가하기](https://user-images.githubusercontent.com/34112237/97229731-2727c200-181c-11eb-8b22-397e72991db2.PNG)

### 03. 어그리게잇으로 묶기
![03 어그리게잇으로 묶기](https://user-images.githubusercontent.com/34112237/97229738-2a22b280-181c-11eb-822a-0626222aa12e.PNG)

### 04. 바운디드 컨텍스트로 묶기
![04 pub sub하기전](https://user-images.githubusercontent.com/34112237/97229746-2bec7600-181c-11eb-9639-bb12758c4585.PNG)

### 05-1. 모델 수정 - 주문 취소3개 삭제
![05 주문취소3개없애야함](https://user-images.githubusercontent.com/34112237/97229755-2e4ed000-181c-11eb-871b-63c4817266ca.PNG)

### 05-2. 모델 수정 - 주문취소접수,알림발송됨 삭제
![06 주문취소접수,알림발송됨 삭제해야함](https://user-images.githubusercontent.com/34112237/97229765-30b12a00-181c-11eb-95b2-89ba111ee328.PNG)

### 05-3. 모델 수정 - REQ/RES 적용
![07 주문됨에서 바로 주문접수되면안됨](https://user-images.githubusercontent.com/34112237/97229771-33138400-181c-11eb-87cd-d8187a1b4ab1.PNG)

### 06. 팀과제최종
![08 팀과제최종](https://user-images.githubusercontent.com/34112237/97229777-34dd4780-181c-11eb-9cff-c61059a46bd8.PNG)

### 06. 팀과제최종(영어)
![09 팀과제최종영어](https://user-images.githubusercontent.com/34112237/97229782-36a70b00-181c-11eb-9417-f3c387f84689.PNG)

### 1차 모형 기능적/비기능적 요구사항을 만족하는지 검증
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
    1. 주문과 조회를 분리하여 시스템 성능을 향상시킨다.  (CQRS) (OK)

## 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/34112237/97233785-dcf60f00-1822-11eb-9583-abc3d6ae706e.png)

# 구현
## 실행 결과
![CancelLog(External request)](https://user-images.githubusercontent.com/34112237/97390068-4b1ffc00-191f-11eb-98c2-563bed04de5f.png)
![CancelMessage](https://user-images.githubusercontent.com/34112237/97390071-4d825600-191f-11eb-88e3-3f777e4ed091.png)
![CancelSTEP01-POST](https://user-images.githubusercontent.com/34112237/97390075-4fe4b000-191f-11eb-92fb-300535fc0f18.png)
![CancelSTEP02-PATCH](https://user-images.githubusercontent.com/34112237/97390078-51ae7380-191f-11eb-8543-8b3fa9aefb1b.png)
![CoreMessage](https://user-images.githubusercontent.com/34112237/97390086-5541fa80-191f-11eb-89db-e092a9e962ec.png)
![CoreSTEP01-POST](https://user-images.githubusercontent.com/34112237/97390090-58d58180-191f-11eb-910a-6c80dae95fa7.png)
![CoreSTEP02-PATCH](https://user-images.githubusercontent.com/34112237/97390093-5b37db80-191f-11eb-9ca8-2f49d30cfc8f.png)
![CoreSTEP03-PATCH](https://user-images.githubusercontent.com/34112237/97390100-5e32cc00-191f-11eb-9f8e-2c28650b65cc.png)
![CoreSTEP04-PATCH](https://user-images.githubusercontent.com/34112237/97390106-60952600-191f-11eb-88db-5494b8a5c584.png)


## 서비스 호출
### Pizza Order 사용법
주문
```
http POST localhost:8088/pizzaOrders customerId=10 state="PLACE" menuOption="pepperoniPizza" price=10000 paymentMethod="CreditCard" address="ThewellI007Ho"
```
주문 취소
```
http PATCH localhost:8088/pizzaOrders/1 state="CANCEL"
```
   
### Payment 사용법


### OrderDelivery 사용법
피자제작시작 입력
```
http PATCH localhost:8088/orderDeliveries/1 state="PizzaProductionStarted"
```
배송출발 입력
```
http PATCH localhost:8088/orderDeliveries/1 state="DeliveryStarted"
```
배송완료 입력
```
http PATCH localhost:8088/orderDeliveries/1 state="DeliveryCompleted"
```

### 주문 취소
```
http PATCH localhost:8088/pizzaOrders/1 state="CANCEL"
```

## DDD의 적용
간략한 설명 작성
```
소스코드 붙여넣기
```

## 폴리글랏 퍼시스턴스
간략한 설명 작성
```
소스코드 붙여넣기
```

## 폴리글랏 프로그래밍
간략한 설명 작성
```
소스코드 붙여넣기
```

## 동기식 호출과 Fallback 처리
간략한 설명 작성
```
소스코드 붙여넣기
```

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트
간략한 설명 작성
```
소스코드 붙여넣기
```
# 운영

## CI/CD 설정
간략한 설명 작성
```
소스코드 붙여넣기
```

## 동기식 호출 / 서킷 브레이킹 / 장애 격리
간략한 설명 작성
```
소스코드 붙여넣기
```

### 오토 스케일 아웃
k8s hpa를 활용한 auto scaling

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
1. hpa 생성

## 무정지 배포
간략한 설명 작성
```
소스코드 붙여넣기
```
# deployment.yaml 의 readiness probe 의 설정:








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


## Autoscale (HPA)
사용자의 요청을 모두 받아들이기 위해 Auto Scale 기능을 적용했다. 


- 주문배송 관리 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 20프로를 넘어서면 replica 를 최대 10개까지 늘려준다:
```
kubectl autoscale deployment orderdeliverymanagement --cpu-percent=20 --min=1 --max=10
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
watch -n 3 kubectl get all
```
![auto scale  늘어나기 전 pods list](https://user-images.githubusercontent.com/44703764/97434966-22215a80-1963-11eb-8bfe-a32d7f9cfcf4.png)

- 무한 워크로드를 걸어준다.
```
while true;do curl 52.149.189.108:8080/orderDeliveries;done
```

- cpu 사용량 조회를 위해 hpa를 모니터링한다.
```
watch -n 1 kubectl get hpa
```
![auto scale  fullload - replica 10으로 늘어남](https://user-images.githubusercontent.com/44703764/97434956-1df53d00-1963-11eb-9e39-2cf8aa5f49e3.png)

- cpu 사용량이 20%를 넘어가면서 스케일 아웃이 벌어지는 것을 확인할 수 있다:
![auto scale  load target 초괴하여 replica 늘어남](https://user-images.githubusercontent.com/44703764/97434963-1f266a00-1963-11eb-9081-8fb8bcd7ae48.png)

- ![auto scale  fullload - replica 10으로 늘어난 pods list](https://user-images.githubusercontent.com/44703764/97434995-2d748600-1963-11eb-81ac-8afeef211f08.png)

- 무한 로드 중단 후 cpu 사용량이 안정되면 replica가 줄어드는 것을 확인할 수 있다:
![auto scale  부하정상화된 후 1로 줄어듦](https://user-images.githubusercontent.com/44703764/97434949-1c2b7980-1963-11eb-9e2a-56509717cf6a.png)



## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 설정을 제거함

- seige를 사용하여 CI/CD 실행 직전에 워크로드를 모니터링 함.
```
siege -c50 -t60S -r10 http://10.0.94.200:8080/pizzaOrders
```

- git commit을 하여 CI/CD 파이프라인 시작
- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
![readiness  readiness없을 때 배포 시 가용성 낮음](https://user-images.githubusercontent.com/44703764/97436892-0e2b2800-1966-11eb-8c5e-e66da089c099.png)


배포기간중 Availability 가 평소 100%에서 80% 대로 떨어지는 것을 확인.
원인은 쿠버네티스가 새로 올려진 서비스의 상태를 무조건 READY로 인식하여 서비스 유입을 성급하게 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함:

- deployment.yaml 을 수정하여 readiness probe 설정한 후 git commit.

- 동일한 시나리오로 재배포 한 후 Availability 확인:
![readiness  설정 후 무중단 배포된 것 확인함](https://user-images.githubusercontent.com/44703764/97436897-0ec3be80-1966-11eb-8a6e-99b02482995a.png)


배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


## Zero-downtime deploy (Readiness Probe)
- 배포가 될 때 무정지로... 부하 중에 새로운 버전으로....

## Self-healing (Liveness Probe)
- 리스타트되도록 증적 캡쳐	

![image](https://user-images.githubusercontent.com/34112237/97389585-047dd200-191e-11eb-8a2d-11639b262d76.png)

![image](https://user-images.githubusercontent.com/34112237/97389810-a4d3f680-191e-11eb-9635-d01352b6aa07.png)


## Config Map / Persistence Volume (둘 중에 하나)
- application.yml

![image](https://user-images.githubusercontent.com/34112237/97435458-c86d6000-1963-11eb-8c36-1debcb95ae40.png)
- deployment.yml

![image](https://user-images.githubusercontent.com/34112237/97435479-d0c59b00-1963-11eb-844f-e593772a39a1.png)
- configmap 설정

![image](https://user-images.githubusercontent.com/34112237/97435518-dd49f380-1963-11eb-9592-8c7c16bab8f3.png)
- Application 활용

![image](https://user-images.githubusercontent.com/34112237/97435550-e8048880-1963-11eb-8434-1325a51c2868.png)

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
