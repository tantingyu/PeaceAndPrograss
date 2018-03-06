# PeaceAndPrograss

<h2>Purpose:<br></h2>
<p>This program first generates a directed acyclic graph of the processes showing parent/child relationship, and then traverses down the graph to execute the processes in parallel, taking into account the data and control dependencies. Child processes only start executing when their parents have finished.

<h2>How To Compile:<br></h2>
<h4>Step 1: Downloading the code <br></h4>
<p>To clone this github repo to your local storage, you can either download the zip folder and extract the contents to your preferred folder, or use your terminal to clone the repo. <br> 
Here's a helpful guide: https://help.github.com/articles/cloning-a-repository/ </p>

<h4>Step 2: Before running the code</h4>
<p>There are several test cases included for you to try from: <code>"graph-file"</code>,<code>"graph-file1"</code>,<code>"graph-file2"</code>, <code>"test1.txt"</code>,<code>"test2.txt"</code><br>
	<b>For Linux:</b><br>
	If you prefer to run the script in your terminal in linux,<br>
	<ul>
		<li> Go to ProcessManagement.java</li>
		<li> Change the file path from <code>"graph-file"</code> to <code>args[0]</code> as shown below: <br>
			<a href="https://ibb.co/g0RPOS"><img src="https://preview.ibb.co/mXEpq7/1.png" alt="1" border="0"></a><a href="https://ibb.co/cfdFV7"><br>
			<img src="https://preview.ibb.co/d4Wr3S/2.png" alt="2" border="0"></a>
<br>
			and when running in the terminal, enter the file name of the test case you want to use.</li>
		<li> Then, remove the line <code>package justice;</code> from all the .java files</li>
		</ul></p>
<p>	
<b>For IDE: </b><br>
If you are running it from an IDE (e.g. Eclipse, Intellij etc), you can just go to ProcessManagement.java and change the <code>"graph-file"</code> to a different test case.</p>

<h4>Step 3: Running the code</h4>
<p><b> For Linux: </b><br>
From the terminal, cd to the folder you stored the code in. Then compile the java file, and run the file.<br>
E.g.<br>
<ul>
<code>$ cd "/home/user/PeaceAndPrograss/src/justice"</code><br>
<code>$ javac ProcessManagement.java</code><br>
<code>$ java ProcessManagement</code>
</ul>
<p>If you receive a "File not found" error for the test case, you can try moving the test case from  the PeaceandPrograss folder to the PeaceAndPrograss/src/justice folder. <br></p>

<h2>What It Does:</h2>
