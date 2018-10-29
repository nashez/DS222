The file contains instructions to deploy and run this assignment.

Data Path: For convenience, please keep all the data in a folder named 'data' at this level, otherwise you need to change all the names of the data paths. Used filenames are as follows:
Training Data : train.txt
Development: devel.txt
Testing Data: test.txt
Also make sure there is a folder named 'models' to store the checkpoint values for the models
Attached to this there is a folder named 'logs' which contains the records of the experiments' onscreen outputs

1. Run the 'preprocess.py' script in the 'code' folder. Make note of the vocabulary size and class size displayed and update these variables in all of the '.py' files
2. For Local training:
	a. Constant rate: run the 'local_const_train.py'
	b. Incrementing rate: run the 'local_increm_train.py'
	c. Decrementing rate: run the 'local_decrem_train.py'
3. For testing, the last few lines of each of above files should be uncommented. Although there is also a separate 'test_model.py' which can also be used
4. For Distibuted training:
