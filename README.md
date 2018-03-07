# PeaceAndPrograss

<<<<<<< HEAD
Purpose:
=======
<p> Programming Assignment 1<br>
Authors: Tan Ting Yu (1002169) and Chong Lok Swen (1002468)<br>
Date: 06/03/2018<br>
</p>
 
<h2>Purpose<br></h2>
<p>This program first generates a directed acyclic graph of the processes showing parent/child relationship, and then traverses down the graph to execute the processes in parallel, taking into account the data and control dependencies. Child processes only start executing when their parents have finished.
>>>>>>> branch 'lok' of https://github.com/tantingyu/PeaceAndPrograss.git

How To Compile:

<<<<<<< HEAD
What It Does:
=======
<h4>Step 2: Accessing input files</h4>
<p>There are several test cases in <samp>"src/justice/testinputs"</samp> included for you to try from: <code>"graph-file"</code>,<code>"graph-file1"</code>,<code>"graph-file2"</code>, <code>"test1.txt"</code>,<code>"test2.txt"</code><br>
Remember to move the test input file into the <samp>/justice</samp> folder before running the code! <br>
Additionally, you can add your own input files in this format:<code>&lt;program name with arguments&gt; : &lt;child nodes IDs&gt; : &lt;input&gt; : &lt;output&gt;</code><br>
</p>

<h4>Step 3: Running the code</h4>
From the terminal, cd to the folder you stored the code in. Then compile the java file, and run the file with the file name of your input file.<br>
For Example:
<pre><code>$ cd "/home/user/PeaceAndPrograss/src/justice"
$ javac ProcessManagement.java
$ java ProcessManagement test1.txt
</code></pre>
<p>If you receive a "File not found" error, make sure that the input file is in PeaceAndPrograss/src/justice folder. <br></p>

<h2>What It Does</h2>
<p>
	<ul>
		1. The input file is written in this format:<code>&lt;program name with arguments&gt; : &lt;child nodes IDs&gt; : &lt;input&gt; : &lt;output&gt;</code><br>
		2. The program parses the input file to generate a ProcessGraph, which prints out details of nodes under <samp>Graph info:</samp><br><a href="https://imgbb.com/"><img src="https://image.ibb.co/cqdP7n/3.png" alt="3" border="0"></a><br>
		3. The program enters a loop to traverse all the nodes in the graph, keeping a record of unexecuted nodes. <br>
		4. The executed and runnable status of each node is printed under <samp>Basic Info:  </samp><br>
<a href="https://ibb.co/kSOD07"><img src="https://image.ibb.co/m6y2tS/4.png" alt="4" border="0"></a><br>
		5. Of the unexecuted nodes, the ones that are runnable will be executed with a new thread. The program keeps track of threads that are currently running but not executed yet, by checking whether the thread was newly created or not.  <br>
		6. Threads that are finished executing will no longer be included as an unexecuted node in the next loop.<br>
		7. The <samp>Basic Info</samp> will be updated with the status of all the nodes.<br>
		8. When the list of unexecuted nodes is empty, the program will exit the loop as all processes are executed and successfully run. <br>
		9. Upon completion of all threads, the system will print <samp>"All processes finished successfully"</samp>  <br>
	</ul>
</p>
>>>>>>> branch 'lok' of https://github.com/tantingyu/PeaceAndPrograss.git
