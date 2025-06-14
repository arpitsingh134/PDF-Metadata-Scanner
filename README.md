Here’s an updated and complete version of your `README.md` for the **PDF Metadata Scanner API**, now with support for MySQL, Docker Compose, asynchronous processing, and enhanced instructions.

---

# 📄 PDF Metadata Scanner API

This Spring Boot project allows you to:

* Upload PDF files
* Generate SHA256 hashes
* Asynchronously extract and store metadata in a MySQL database
* Retrieve metadata by hash

## 🚀 Features

* 📦 RESTful endpoints for uploading and retrieving PDFs
* 🔐 SHA256-based hash identification
* ⚙️ Asynchronous metadata extraction
* 🗃️ MySQL persistence
* 🐳 Docker & Docker Compose support
* 📝 Clean logs for traceability

---

## 🔧 Prerequisites

* Docker + Docker Compose installed
* Optional: Java 17+ and Maven if running locally

---

## 🐳 Docker Setup (Recommended)

### Clone Repo

```bash
git clone https://github.com/arpitsingh134/PDF-Metadata-Scanne.git
cd pdf-metadata-scanner
```

### Run with Docker Compose

#### Start Docker Containers
```bash
docker-compose up --build
```

#### Stop Docker Containers
```bash
docker-compose down -V
```

---
### Check database data after running docker containers 
```
docker exec -it mysqldb mysql -u testuser -p    
```
#### give password 

```
testpass
```

#### Run command

```
show tables;
use pdfscanner;
show tables;
select * from pdf_metadata;
```


---

## 🛠️ Running Locally (Without Docker)

Make sure MySQL is running and `application.properties` is updated with correct DB credentials.

```bash
mvn clean spring-boot:run
```

---

## 📮 API Usage

### 🔼 Upload PDF

Uploads the file and triggers metadata extraction asynchronously.

```bash
curl -F "file=@sample.pdf" http://localhost:8080/scan
```

#### ✅ Response:

```json
{
  "sha256": "hqR2EoK/q7NQ0/DGXAJfI/Da8mqwYZcD3TxA/pdKX1Y="
}
```

---

### 🔍 Lookup Metadata

```bash
curl http://localhost:8080/lookup/hqR2EoK%2Fq7NQ0%2FDGXAJfI%2FDa8mqwYZcD3TxA%2FpdKX1Y%3D
```

#### ✅ Example Response:

```json
{
  "sha256": "hqR2EoK/q7NQ0/DGXAJfI/Da8mqwYZcD3TxA/pdKX1Y=",
  "version": "1.7",
  "producer": "Apache PDFBox",
  "author": "Arpit Singh",
  "created": "D:20250614010000Z",
  "modified": "D:20250614020000Z",
  "scanned": "2025-06-14T05:45:00Z",
  "filename": "sample_20250614_114500.pdf"
}
```

---

## 🧾 Project Structure

| Layer        | Description                          |
| ------------ | ------------------------------------ |
| `controller` | Handles `/scan` and `/lookup/{hash}` |
| `service`    | Extracts metadata using PDFBox       |
| `model`      | PdfMetadata entity                   |
| `repository` | Spring Data JPA for MySQL            |
| `util`       | SHA256 Hash Utility                  |

---

## ⚙️ Tech Stack

* Java 17
* Spring Boot
* Spring Web + Spring Data JPA
* MySQL
* Apache PDFBox
* Docker / Docker Compose
* Lombok + SLF4J

---

## 📁 Files to Note

* `Dockerfile`: For containerizing the Spring Boot app
* `docker-compose.yml`: Spins up Spring app + MySQL DB
* `application.properties`: DB configs and JPA tuning
* `PdfMetadata`: JPA entity model for metadata

---

## 👨‍💻 Author

**Arpit Singh**
📧 [arpitsingh134@gmail.com](mailto:arpitsingh134@gmail.com)
🔗 [LinkedIn](https://linkedin.com/in/arpitsingh134)

---