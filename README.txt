
LIBRARY MANAGEMENT SYSTEM (PHASE 1)

PREREQUISITES

(JDK) 1.8 or higher

Eclipse IDE for Enterprise Java and Web Developers

Maven

how to run ?

Import the project into Eclipse as a Maven Project.

For Security Requirement:
This system does not hardcode passwords. You must set these in Eclipse:


 Go to the "Environment" tab.
 add:

1 Variable: LIBRARY_ADMIN_USER

Value: admin


2 Variable: LIBRARY_ADMIN_PASS

Value:Mar@789

Run.

use the menu to use the app.


FEATURES IMPLEMENTED


Admin Login/Logout (Secure)

Add Books

Search Books (Title, Author, ISBN)

Borrow Books (28-day loan period)

Overdue Detection (Time travel simulation)

Observer Pattern for Notifications (Mock Email)

JUnit 5 Testing & Mockito Integration

DESIGN PATTERNS USED

Strategy Pattern: Used for calculating fines (BookFineStrategy, CDFineStrategy).

Observer Pattern: Used for sending notifications (Subject/Observer).

Layered Architecture: Strict separation of Presentation, Service, Domain, and Repository.