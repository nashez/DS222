import tensorflow as tf
import numpy as np
import sys
import random
import time

#Fixed parameters
class_count = 50
#learning_rate = 0.01
#small dataset
vocab_size = 49110
#large dataset
#vocab_size = 260826
regularizer = 0.001

#Weights to be learned
weights = tf.Variable(tf.zeros([vocab_size,class_count]))
#Holds The feature vector of data instance
feature_data_instance = tf.placeholder(tf.int32, shape=(None))
learning_rate = tf.placeholder(tf.float32,shape=())
#Holds values for true labels
known_label = tf.placeholder(tf.float32, shape=(class_count,))

known_label_value = tf.placeholder(tf.int32,shape=(1))

length = tf.placeholder(tf.int32,shape=(1))
#Weights for only the concerned features for this data instance
requiredWeights = tf.transpose(tf.gather(weights,feature_data_instance))
#Summation of all values in column of requiredweight instances
sums = tf.exp(tf.reduce_sum(requiredWeights, 1))
#probability calculated for all possible requiredweights values
probab_distrib = tf.divide(sums,tf.reduce_sum(sums))
#calculate gradient values
gradient_values = tf.subtract(known_label,probab_distrib)
inter = tf.tile(tf.transpose(gradient_values),[length[0]])
interx = tf.reshape(inter,[length[0],class_count]) * learning_rate
intery = tf.add(interx,2 * regularizer * learning_rate * tf.transpose(requiredWeights))
#weights = tf.subtract(weights,2*learning_rate*regularizer*weights)
update_gradient_values = tf.scatter_add(weights,feature_data_instance,intery)
#tf.assign(weights,update_gradient_values)
sums_red = tf.reduce_sum(sums)
probs = tf.divide(sums,sums_red)
sums_red1 = tf.gather(sums,known_label_value)
inter_loss = tf.divide(sums_red1,sums_red)
loss = tf.add(tf.multiply(tf.log(inter_loss),-1),regularizer*tf.norm(weights,2))

#print(gradient_values.shape)
saver = tf.train.Saver([weights])
init_op = tf.global_variables_initializer()
def main(_):
        #print(gradient_values,[length[0]])
        arrays_train = []
        arrays_validate = []
        arrays_test = []
        asso_class_train = []
        asso_class_validate = []
        asso_class_test = []
        lrate = 0.01
        #load training dataset
        
        with open("../data/final_train.txt","r") as f:
                for line in f:
                        parts = line.strip().split("\t")[1].split(",")
                        s = set(parts)
                        a = list(s)
                        arrays = np.array(a,dtype=np.int32)
                        arrays = arrays[arrays>=0]
                        arrays_train.append(arrays)
                        #print(arrays)
                        parts = line.strip().split("\t")[0].split(",")
                        asso_class_train.append(np.array(parts[0],dtype=np.int32))
        #load validation dataset
        with open("../data/final_devel.txt","r") as f:
                for line in f:
                        parts = line.strip().split("\t")[1].split(",")
                        s = set(parts)
                        a = list(s)
                        arrays = np.array(a,dtype=np.int32)
                        arrays = arrays[arrays>=0]
                        arrays_validate.append(arrays)
                        #print(arrays)
                        parts = line.strip().split("\t")[0].split(",")
                        asso_class_validate.append(np.array(parts[0]))
        
		with tf.Session() as sess:

                sess.run(init_op)
                
                err = 0
                loss_itr = 0
                for step in range(0,len(arrays_validate)-1):
                #for step in range(0,5000):
                        val_rec = sess.run([loss,probs],feed_dict={feature_data_instance:arrays_validate[step], learning_rate:lrate, known_label_value: asso_class_validate[step].reshape(1),length:arrays_validate[step].shape})
                        loss_itr += val_rec[0]
                        numpyarray = val_rec[1]
                        list1 = np.argmax(numpyarray).tolist()
                        list2 = asso_class_validate[step].tolist()
                        if str(list1) == str(list2):
                                err = err
                        else:
                                err += 1
                print("Error before training " + str(float(err)/len(arrays_validate)))

                iteration = 0
                while iteration<10:
                        iteration += 1
                        toShuffle = list(zip(arrays_train, asso_class_train))
                        random.shuffle(toShuffle)
                        arrays_train, asso_class_train = zip(*toShuffle)
                        epoch_start_time = time.time()
                        for step in range(0,len(arrays_train)-1):
                                correct = np.zeros(class_count)
                                correct[asso_class_train[step]] = 1
                                start_time = time.time()
                                up,wg = sess.run([update_gradient_values,weights],feed_dict={feature_data_instance:arrays_train[step], learning_rate:lrate, known_label: correct,length:arrays_train[step].shape})
                                #temp1=sess.run(inter,feed_dict={feature_data_instance:arrays_train[step], known_label: correct,length:arrays_train[step].shape})
                                #print(wg)
                                #print(up)
                                #print('Gradients\n'+update_gradient_values)
                                end_time   = time.time()
                                #print("Time for processing each tuple is " + str(end_time-start_time))
                        epoch_end_time = time.time()
                        #print(str(weights[0][0]))
                        #print(str(update_gradient_values[0][0]))
                        print("Iteration = "+ str(iteration)+"\nEpoch Time = " + str(epoch_end_time - epoch_start_time))
                        save_path = saver.save(sess, "models/model_sml_const.ckpt")


                        err = 0
                        loss_itr = 0
                        for step in range(0,len(arrays_validate)-1):
                                val_rec = sess.run([loss,probs],feed_dict={feature_data_instance:arrays_validate[step], learning_rate:lrate, known_label_value: asso_class_validate[step].reshape(1),length:arrays_validate[step].shape})
                                loss_itr += val_rec[0]
                                numpyarray = val_rec[1]
                                list1 = np.argmax(numpyarray).tolist()
                                list2 = asso_class_validate[step].tolist()
                                if str(list1) == str(list2):
                                        err = err
                                else:
                                        err += 1
                        print("\nError = " + str(float(err)/len(arrays_validate)) + "\nLoss = " + str(loss_itr[0]))
                '''
				with open("../data/final_test.txt","r") as f:
					for line in f:
                        parts = line.strip().split("\t")[1].split(",")
                        s = set(parts)
                        a = list(s)
                        arrays = np.array(a,dtype=np.int32)
                        arrays = arrays[arrays>=0]
                        arrays_test.append(arrays)
                        #print(arrays)
                        parts = line.strip().split("\t")[0].split(",")
                        asso_class_test.append(np.array(parts[0]))
        
                saver.restore(sess, "models/model_sml_const.ckpt")
                err = 0
                loss_itr = 0
                for step in range(0,len(arrays_test)-1):
                        val_rec = sess.run([loss,probs],feed_dict={feature_data_instance:arrays_test[step], learning_rate:lrate, known_label_value: asso_class_test[step].reshape(1),length:arrays_test[step].shape})
                        loss_itr += val_rec[0]
                        numpyarray = val_rec[1]
                        list1 = np.argmax(numpyarray).tolist()
                        list2 = asso_class_test[step].tolist()
                        if str(list1) == str(list2):
                                err = err
                        else:
                                err += 1
                print("\nTesting Error = " + str(float(err)/len(arrays_test)) + "\nLoss = " + str(loss_itr[0]))
				'''
                #save_path = saver.save(sess, "models/model_full_const.ckpt")
                #print("Saved Model after training")
#               sess.close()

#       with tf.Session as sess:


if __name__ == "__main__":
        tf.app.run(main=main)
