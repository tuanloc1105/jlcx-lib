Thực hiện 1 app todo. Lưu ý rằng các API response đều có kiến trúc như sau

```json
{
    "data": {},
    "trace": "019b3e71f5dc78e0b225c85814c28d40",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```

Các API có sẵn như sau

## 1. Đăng ký người dùng

Tại API `/api/users/create_new`

Request trông như sau:

```json
{
    "fullName": "Võ Tuấn Lộc",
    "username": "tuanloc2",
    "password": "123123"
}
```

---

Response trông như sau:

```json
{
    "trace": "019b3e8245c3764db2cdd9e5c05dfb48",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```

## 2. Đăng nhập

POST API `/api/users/login`

Request trông như sau:

```json
{
    "username": "tuanloc",
    "password": "123123"
}
```

---

Response trông như sau:

```json
{
    "data": {
        "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpZCI6NSwidXNlcm5hbWUiOiJ0dWFubG9jIiwiZnVsbE5hbWUiOiJWw7UgVHXhuqVuIEzhu5ljIiwiaWF0IjoxNzY2MjgwMzY0LCJleHAiOjE3NjcxNDQzNjR9.r6ffc0W5kXoxwVXWzajOfFLgkVa_0RnCgJwYgt1YBqSDTS3o8KuefmcFVeHAZjVw4rd-Uc-LcsoeK3pEiuJoKHMkDJCp7_0dw4eaLpOHQ_zBmOk7Pki090rWhbm2UsntAwwazY9UTq-bQHjwYZaEtmcfdGRIa1kdOp3xckn6TzpUvW7DNOtpIncNC5EZfDZnRjdg03WzEtyStm5c3SjLolbSL9aw91x_5hS3OKaRmO6xx0EOC0RWFQbl--7sOPXtmAgV7FMGLCk9Fxog0oCH7-8DQdR-D-FBLhoK291FC9mnfq0zp_xQ--DNp74W90Zjul0lxO4iL4AgfxUY56FbPQ",
        "userInfo": {
            "username": "tuanloc",
            "fullName": "Võ Tuấn Lộc",
            "createdAt": "2025-12-14 22:17:50.853",
            "updatedAt": "2025-12-14 22:17:50.853"
        }
    },
    "trace": "019b3e838163791ca09661d1ee3a470d",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```

## 3. Tạo 1 todo mới (có truyền bearer access token)

POST API `/api/tasks/create_task`

Request trông như sau:

```json
{
    "taskTitle": "task title 6817b59a-9980-45b6-ba2f-3d7487dc9dc8",
    "taskDetail": "task detail f22d018a-c825-4b82-847e-e551b07ffe6f"
}
```

---

Response trông như sau:

```json
{
    "trace": "019b3e8dcc6c7a3f908a918314770955",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```

## 4. Lấy danh sách todo (có truyền bearer access token)

POST API `/api/tasks/get_all_task`

Request trông như sau:

```json
{
    "pageNumber": 1
}
```

Lưu ý `pageNumber` luôn luôn bắt đầu từ 1

---

Response trông như sau:

```json
{
    "data": {
        "pageNumber": 1,
        "pageSize": 10,
        "totalPages": 1,
        "numberOfElements": 1,
        "totalElements": 1,
        "firstPage": true,
        "lastPage": true,
        "content": [
            {
                "id": 1,
                "taskTitle": "task title 6817b59a-9980-45b6-ba2f-3d7487dc9dc8",
                "taskDetail": "task detail f22d018a-c825-4b82-847e-e551b07ffe6f",
                "finished": false,
                "createdAt": "2025-12-21 08:37:18.999",
                "updatedAt": "2025-12-21 08:37:18.999"
            }
        ]
    },
    "trace": "019b3e8ff6757b339a394cecf272bd79",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```

## 5. Lấy chi tiết todo (có truyền bearer access token)

POST API `/api/tasks/get_task_detail`

Request trông như sau:

```json
{
    "id": 1
}
```

---

Response trông như sau:

```json
{
    "data": {
        "id": 1,
        "taskTitle": "task title 6817b59a-9980-45b6-ba2f-3d7487dc9dc8",
        "taskDetail": "task detail f22d018a-c825-4b82-847e-e551b07ffe6f",
        "finished": false,
        "createdAt": "2025-12-21 08:37:18.999",
        "updatedAt": "2025-12-21 08:37:18.999"
    },
    "trace": "019b3e9b38e375b192a8cfe618cb5de2",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```

## 6. Tìm todo theo tên (có truyền bearer access token)

POST API `/api/tasks/search_task_by_name`

Request trông như sau:

```json
{
    "pageNumber": 1,
    "searchContent": "task"
}
```

Lưu ý `pageNumber` luôn luôn bắt đầu từ 1

---

Response trông như sau:

```json
{
    "data": {
        "pageNumber": 1,
        "pageSize": 10,
        "totalPages": 1,
        "numberOfElements": 1,
        "totalElements": 1,
        "firstPage": true,
        "lastPage": true,
        "content": [
            {
                "id": 1,
                "taskTitle": "task title 6817b59a-9980-45b6-ba2f-3d7487dc9dc8",
                "taskDetail": "task detail f22d018a-c825-4b82-847e-e551b07ffe6f",
                "finished": false,
                "createdAt": "2025-12-21 08:37:18.999",
                "updatedAt": "2025-12-21 08:37:18.999"
            }
        ]
    },
    "trace": "019b3ea185a67f0098ca933ba270150e",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```

## 7. Cập nhật todo (có truyền bearer access token)

POST API `/api/tasks/update_task`

Request trông như sau:

```json
{
    "id": 1,
    "taskTitle": "task title update to 6578654c-f820-437e-a4aa-5b0d39787633 at 2025-12-21 09:05:06.457037+07:00",
    "taskDetail": "task detail update to bc496dc6-f293-4735-aa58-3f05d6954973 at 2025-12-21 09:05:06.457053+07:00",
    "finished": false
}
```

---

Response trông như sau:

```json
{
    "trace": "019b3ea73e73784990e95c5e31bddbc0",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```

## 8. Xoá todo (có truyền bearer access token)

POST API `/api/tasks/delete_task`

Request trông như sau:

```json
{
    "id": 1
}
```

---

Response trông như sau:

```json
{
    "trace": "019b3ea8d1d97ad6bb1ff092905f62cf",
    "errorCode": 100000,
    "errorDescription": "Success",
    "httpCode": 200
}
```
