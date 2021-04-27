====
    Copyright (c) 2020 The JavaSSH Project

     Permission is hereby granted, free of charge, to any person obtaining a copy
     of this software and associated documentation files (the "Software"), to deal
     in the Software without restriction, including without limitation the rights
     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     copies of the Software, and to permit persons to whom the Software is
     furnished to do so, subject to the following conditions:

     The above copyright notice and this permission notice shall be included in
     all copies or substantial portions of the Software.

     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
     THE SOFTWARE.
====

If you want you use Maverick with these examples, there are two things you need to do.

1. Add maverick-all.jar to the the classpath. 

drop your license in this directory
as a file named maverick-license.txt.

Alternatively, place it in the directory you will run your application from as a file
name .maverick-license.txt (notice the preceding '.'). 


Building From Source (Maven)
============================

1. Download and extract the Maverick distribution somewhere. By default, Maven expects
   to find it at $HOME/maverick.

2. Set the property "maverickHome". Either pass it in as a property when you run Maven, or 
   edit ../sshapi-group/pom.xml and change it there.
   
This should then allow you to compile the Maverick bridge

Including The License
=====================

As Maverick is a commercial library, for it to work you require a license file. There are
a number of ways you can install the license.

1. In you Java code. Just use the usual method Maverick employs. This will however mean
   you application will be dependent on Maverick, and it is up to you to cope with
   this gracefully.
   
2. Place the license in a file called maverick-license.txt and place it at the root of
   one of your classpath resources, be it a directory a jar file.
   
3. Place the license in a file called .maverick-license.txt (notice the dot at the 
   start of the file name), in the directory your application runs from.