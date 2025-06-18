# **KJC-SDC-TASKS**

## **Day 2 Tasks (Evening)**

### **1. Create a Maven project titled `Day2<regno>` and perform the following:**

#### **a) Add the following dependencies:**
- **Apache Log4j SLF4J Binding**
- **Apache Commons CLI**

#### **b) Library Management System (Inheritance):**
Design a Java program that models a **library management system**.
- Create a base class named `Book`.
- Create subclasses like `FictionBook` and `NonFictionBook`.

#### **c) Basic Banking System (Exception Handling):**
Design a Java program that simulates a **basic banking system** with the following features:
- Create accounts
- Deposit money
- Withdraw money
- Check balance

Implement **exception handling** for:
- Overdrafts
- Negative transactions
- Non-existent accounts


## **Day 3 Tasks (Afternoon)**

Create a Java application that manages **student enrollments in courses using MongoDB**.

Use **embedded documents** for some data and **referenced documents** for others to understand their structural differences.

📁 **Upload the code to GitHub with the name:** `Day3-Task`

---

### **Requirements**

#### **MongoDB Collections**
- `students` — stores student details
- `courses` — stores course details
- `enrollments` — stores enrollment records

---

### **Document Structure**
In the `enrollments` collection:
- One document should **embed** both student and course data.
- Another should **reference** them using `ObjectId`.

---

### **To Do:**

1. **Insert sample students and courses.**
2. **Add two types of enrollments:**
   - One using **embedded documents**
   - One using **referenced documents**
3. **Query and print both types** with full details.
4. **Update a student's name** and:
   - **Mention the difference** between updating a **referenced document** vs an **embedded document**.
5. **Create indexes** for querying the `students` collection.
6. **Share screenshots** of the result in the README file.

---

✅ Example code, screenshots, and documentation to be added in respective folders/files.

