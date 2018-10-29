
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
        arrays_test = []
        asso_class_test = []
        with open("../data/final_test.txt","r") as f:
                for line in f:
                        parts = line.strip().split("\t")[1].split(",")
                        s = set(parts)
                        a = list(s)
                        arrays = np.array(a,dtype=np.int32)
                        arrays = arrays[arrays>=0]
                        arrays_test.append(arrays)
                        parts = line.strip().split("\t")[0].split(",")
                        asso_class_test.append(np.array(parts[0],dtype=np.int32))
		with tf.Session() as sess:
                sess.run(init_op)        
                #Modify this name to the saved model's name
                saver.restore(sess, "models/model.ckpt")
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

if __name__ == "__main__":
        tf.app.run(main=main)
