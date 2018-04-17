# Spring - Multipart / Batch messages
Enable multipart / batch request for JAVA Spring.

# Links
Implementation is syntactically inspired by

http://www.rfc-editor.org/rfc/rfc2046.txt

https://tools.ietf.org/id/draft-snell-http-batch-00.html

http://www.odata.org/documentation/odata-version-3-0/batch-processing/



# Example

- startup multipart-example spring-boot:run
- POST localhost:8080/my/own/multipart/endpoint

**Header**
```
Content-Type: multipart/mixed
```

**Body**
```
--batch
Content-Type: application/http
Content-Transfer-Encoding: binary

GET /hello HTTP/1.1
Host: host

--batch
Content-Type: application/http
Content-Transfer-Encoding: binary

GET /greeting?name=Trevor HTTP/1.1
Host: host

--batch
Content-Type: application/http
Content-Transfer-Encoding: binary

GET /greeting?name=Peter&name=Lustig HTTP/1.1
--batch--
```

**Response**
```
--batch
Content-Type: application/http
Content-Transfer-Encoding: binary

HTTP/1.1 200 OK
Content-Type: text/plain;charset=ISO-8859-1
Content-Length: 27

Greetings from Spring Boot!
--batch
Content-Type: application/http
Content-Transfer-Encoding: binary

HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Content-Length: 35

{"id":1,"content":"Hello, Trevor!"}
--batch
Content-Type: application/http
Content-Transfer-Encoding: binary

HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Content-Length: 41

{"id":2,"content":"Hello, Peter,Lustig!"}
--batch--
```
