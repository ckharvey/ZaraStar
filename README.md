# ZaraStar
ZaraStar ERP++

This is the repository for the ZaraStar open-source project. The main site is https://zarastar.xyz

At this time, here is the original source code for the project that started in 1998 and wound-down in 2016. ZaraStar is a comprehensive ERP application principally written in Java on the back-end. Embedded JDBC calls to a MySQL database (running initially on the Apache web server and Tomcat servlet container; but latterly on Tomcat alone), generated HTML and JavaScript. Other run-time support was through CSS and JavaScript loads.

ZaraStar includes support for network printing, network faxing, integrated eMailing, precision business document generation, customised screen layouts, and PDF document and catalog generation; amongst other business-efficient features.

Development ceased in 2016 after I had written 600,000+ lines of code. I have now decided to re-write the entire application (as an open-source project) using the latest technologies (Angular, JavaScript (TypeScript), HTML5, CSS3, ...). To start the process, I have uploaded the Java and JavaScript.

This code is <b>not</b> intended to be compiled and executed. It is still running live (for one organisation) but no support is being offered for new installs.

If you are interested in looking at the code, bear in mind:

- The design grew from the early days of the Internet. Java was launched in 1996 (I was at the launch!). Java was slow and lacked much functionality necessary for business system development. For instance, at the start I needed access to printers connected to PCs... Java had no print support; how to print a sales invoice?

- The Internet was virginal. There was little, if any, experience of <i>how</i> a centralised application running on Internet-based servers could be implemented.

- ZaraStar grew from a system (called OnTop) that I had written in C. The OnTop application was installed on individual PCs and accessed a C-written, non-relational, database running across a Windows network.

The upshot of this is that the Java code was written in a fundamentally different way from which one might choose today. For example, there's a lot of byte-array manipulation; one, because the C code was written that way, and two, because string processing was very slowly in early Java (300 times slower than byte arrays).

<b>C# Update</b>

You'll also find a directory with converted Java to C# code. This is part of a learning program. My son is studying Computer Science at college. The language of instruction is C#, so I'm using the ZaraStar code to help him understand how C# fits in to the wider programming world.

Chris Harvey
September 2018
