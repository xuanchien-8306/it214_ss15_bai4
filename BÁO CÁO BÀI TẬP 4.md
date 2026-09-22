# BÁO CÁO BÀI TẬP 4

## Xây dựng "Nhạc trưởng" Orchestrator Saga với State Machine

## 1. Mục tiêu

Xây dựng mô hình Orchestration Saga sử dụng State Machine để quản lý tập trung quy trình đặt vé concert.

Orchestrator đóng vai trò "nhạc trưởng", quản lý trạng thái giao dịch, gọi các service và quyết định bước tiếp theo dựa trên kết quả trả về.

Các service chính:

* Orchestrator: quản lý State Machine.
* PaymentService: xử lý thanh toán.
* ConcertReservationService: giữ chỗ và gán ghế.

---

## 2. So sánh Choreography và Orchestration

### Choreography Saga

Trong mô hình Choreography, các service tự giao tiếp với nhau thông qua event.

Ví dụ:

```text
BookingService
     ↓ Event
PaymentService
     ↓ Event
ConcertReservationService
```

Ưu điểm:

* Không có service điều phối trung tâm.
* Các service tương đối độc lập.
* Phù hợp với quy trình đơn giản.

Nhược điểm:

* Khi có nhiều service và nhiều event, luồng xử lý trở nên khó theo dõi.
* Khó debug khi xảy ra lỗi.
* Khó biết một giao dịch đang ở bước nào.
* Dễ xảy ra "Event Spaghetti".

### Orchestration với State Machine

Trong Orchestration, có một Orchestrator đứng giữa và điều phối các service.

```text
              Orchestrator
              /          \
             ↓            ↓
      PaymentService   ReservationService
```

Orchestrator quản lý State Machine và biết giao dịch đang ở trạng thái nào.

Ưu điểm:

* Luồng xử lý tập trung, dễ theo dõi.
* Dễ debug.
* Dễ quản lý Retry và Compensation.
* Có thể xác định chính xác trạng thái hiện tại của một giao dịch.

Khi quy trình có nhiều bước hơn, State Machine giúp biểu diễn rõ trạng thái và điều kiện chuyển trạng thái, từ đó giảm sự phức tạp của việc xử lý nhiều event giữa các service.

---

## 3. Các trạng thái của State Machine

Hệ thống sử dụng 6 trạng thái:

| State             | Ý nghĩa                          |
| ----------------- | -------------------------------- |
| INITIATED         | Yêu cầu đặt vé vừa được khởi tạo |
| PAYMENT_PENDING   | Đang xử lý thanh toán            |
| PAYMENT_COMPLETED | Thanh toán thành công            |
| SEAT_RESERVING    | Đang giữ chỗ                     |
| BOOKING_CONFIRMED | Đặt vé hoàn tất                  |
| CANCELLED         | Giao dịch bị hủy                 |

---

## 4. Các sự kiện

Hệ thống sử dụng các event:

| Event               | Ý nghĩa               |
| ------------------- | --------------------- |
| PROCESS_PAYMENT     | Bắt đầu thanh toán    |
| PAYMENT_SUCCESS     | Thanh toán thành công |
| PAYMENT_FAILED      | Thanh toán thất bại   |
| RESERVE_SEATS       | Bắt đầu giữ chỗ       |
| RESERVATION_SUCCESS | Giữ chỗ thành công    |
| RESERVATION_FAILED  | Giữ chỗ thất bại      |

---

## 5. Luồng chuyển trạng thái

### Luồng thành công

```text
INITIATED
    |
    | PROCESS_PAYMENT
    v
PAYMENT_PENDING
    |
    | PAYMENT_SUCCESS
    v
PAYMENT_COMPLETED
    |
    | RESERVE_SEATS
    v
SEAT_RESERVING
    |
    | RESERVATION_SUCCESS
    v
BOOKING_CONFIRMED
```

Đây là luồng chính khi thanh toán và giữ chỗ đều thành công.

### Thanh toán thất bại

```text
INITIATED
    ↓
PAYMENT_PENDING
    ↓ PAYMENT_FAILED
CANCELLED
```

Nếu thanh toán thất bại sau khi đã Retry đủ số lần cho phép, giao dịch chuyển sang `CANCELLED`.

### Giữ chỗ thất bại

```text
PAYMENT_COMPLETED
    ↓
SEAT_RESERVING
    ↓ RESERVATION_FAILED
CANCELLED
```

Trong trường hợp này thanh toán đã thành công nên cần thực hiện Compensation bằng cách hoàn tiền.

---

## 6. Retry Policy

Hệ thống cấu hình Retry cho bước thanh toán:

```text
Maximum attempts: 3
Delay: 2000 ms
```

Khi PaymentService xảy ra timeout, Orchestrator sẽ thử lại tối đa 3 lần.

Ví dụ:

```text
Attempt 1/3
    ↓ Timeout
Chờ 2 giây
    ↓
Attempt 2/3
    ↓ Timeout
Chờ 2 giây
    ↓
Attempt 3/3
```

Nếu một lần thử thành công:

```text
PAYMENT_PENDING
        ↓
PAYMENT_COMPLETED
```

Nếu cả 3 lần đều thất bại:

```text
PAYMENT_PENDING
        ↓
CANCELLED
```

Retry giúp hệ thống có thể xử lý các lỗi tạm thời như timeout mà không cần hủy giao dịch ngay lập tức.

---

## 7. Compensation

Compensation được sử dụng để hoàn tác các bước đã thực hiện khi một bước sau bị lỗi.

Ví dụ:

```text
Thanh toán thành công
        ↓
PAYMENT_COMPLETED
        ↓
Giữ ghế thất bại
        ↓
CANCELLED
        ↓
Refund Payment
```

Orchestrator kiểm tra trạng thái hiện tại để quyết định có cần Compensation hay không.

Nếu thanh toán chưa thành công thì không cần refund.

Nếu thanh toán đã thành công nhưng giữ ghế thất bại thì gọi:

```text
paymentService.refund()
```

Log:

```text
[Orchestrator] Compensation triggered for booking: CONCERT-2026-088
[PaymentService] Refund payment for booking: CONCERT-2026-088
```

---

## 8. Dữ liệu đầu vào

```json
{
  "bookingId": "CONCERT-2026-088",
  "concertCode": "LIVE-HCM-2026-ULTRA",
  "customerId": "VIP-2024",
  "customerEmail": "rika@email.com",
  "ticketQuantity": 3,
  "amount": 5500000
}
```

---

## 9. Hướng dẫn cài đặt và chạy

### Bước 1: Kiểm tra môi trường

Yêu cầu:

```text
Java 21
Gradle
Spring Boot
IntelliJ IDEA
```

### Bước 2: Mở project

Mở folder:

```text
bai4
```

trong IntelliJ IDEA.

### Bước 3: Build project

Windows:

```bash
gradlew.bat clean build
```

Nếu build thành công sẽ không có lỗi compile.

### Bước 4: Chạy project

```bash
gradlew.bat bootRun
```

Ứng dụng chạy tại:

```text
http://localhost:8080
```

---

## 10. Test API

### Test thành công

Gửi:

```text
POST http://localhost:8080/api/bookings?paymentMode=SUCCESS&reservationMode=SUCCESS
```

Body:

```json
{
  "bookingId": "CONCERT-2026-088",
  "concertCode": "LIVE-HCM-2026-ULTRA",
  "customerId": "VIP-2024",
  "customerEmail": "rika@email.com",
  "ticketQuantity": 3,
  "amount": 5500000
}
```

Kết quả mong đợi:

```text
BOOKING_CONFIRMED
```

### Test Payment Timeout

```text
POST http://localhost:8080/api/bookings?paymentMode=TIMEOUT&reservationMode=SUCCESS
```

Kết quả:

```text
Attempt 1/3
Attempt 2/3
Attempt 3/3
CANCELLED
```

### Test Reservation Failure

```text
POST http://localhost:8080/api/bookings?paymentMode=SUCCESS&reservationMode=FAIL
```

Kết quả:

```text
PAYMENT_COMPLETED
SEAT_RESERVING
RESERVATION_FAILED
CANCELLED
Compensation
Refund payment
```

---

## 11. Kết quả chạy thử

Với dữ liệu đầu vào:

```text
Booking ID: CONCERT-2026-088
Concert: LIVE-HCM-2026-ULTRA
Customer: VIP-2024
Ticket quantity: 3
Amount: 5.500.000
```

Luồng thành công:

```text
INITIATED
→ PAYMENT_PENDING
→ PAYMENT_COMPLETED
→ SEAT_RESERVING
→ BOOKING_CONFIRMED
```

Log chính:

```text
[Orchestrator] State: INITIATED -> Event: PROCESS_PAYMENT -> New State: PAYMENT_PENDING
[Orchestrator] RetryPolicy: Activity 'processPayment' - Attempt 1/3
[Orchestrator] State: PAYMENT_PENDING -> Event: PAYMENT_SUCCESS -> New State: PAYMENT_COMPLETED
[Orchestrator] State: PAYMENT_COMPLETED -> Event: RESERVE_SEATS -> New State: SEAT_RESERVING
[Orchestrator] State: SEAT_RESERVING -> Event: RESERVATION_SUCCESS -> New State: BOOKING_CONFIRMED
[Orchestrator] Final State: BOOKING_CONFIRMED for booking CONCERT-2026-088
```

Kết quả cuối cùng:

```text
BOOKING_CONFIRMED
```

---

## 12. Kết luận

Bài tập đã triển khai mô hình Orchestration Saga với State Machine. Orchestrator quản lý tập trung trạng thái và điều phối PaymentService và ConcertReservationService.

State Machine giúp hệ thống dễ theo dõi trạng thái giao dịch, dễ xử lý Retry khi có lỗi tạm thời và thực hiện Compensation khi một bước sau thất bại.

Mô hình này phù hợp hơn khi quy trình Saga có nhiều bước và nhiều trạng thái cần quản lý.
