# Distributed Locking with Spring Boot and MongoDB

## What is locking?

- with locking you regulate access to a specific resource
    - e.g. Locking in Databases, depending on the type of lock only one session/transaction has access to a specific resource e.g.
      a table row or in worst case a complete table
    - only one collaborator can interact with a resource at a time
    - different type of locks:
      - read
      - write
      - probably more I cannot think of right now :)

## When do you need locking?

- when there are multiple parties e.g. threads that all want/need to change(c(r)ud) a shared resource
  - a shared resource may be:
    - a database
    - a cache
    - anything more that one party has write access to

## Locking in a simple Java application

- in Java applications locking is typically done with:
  - synchronized block
  - Locks e.g. ReentrantLock
- does this help with distributed systems?
  - normally no
  - services do not share same jvm so thread locking will not suffice

## Locking in distributed systems

- locking has to be done via a shared resource
  - all participants need to be able to find out who has the actual lock

### What options do we have?

- Option 1: rely on optimistic locking on a shared database
  - let your resource reside on a shared database
  - implement optimistic locking utilizing transactions
  - handle locking exceptions
  - seems error-prone and complicated to me

- Option 2: utilize spring integration distributed locking and use locks like you would in a simple java application
  - lock information resides on a database 
  - based on springs implementation
  - not much effort to implement it
  - not restricted to a database resource
  - I tried this one and will show you now 
