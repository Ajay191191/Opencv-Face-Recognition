#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/contrib/contrib.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include <fstream>
#include <sstream>
#include <android/log.h>

#define LOG_TAG "Ajay"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL Java_org_opencv_face_Sample3View_FindFeatures(JNIEnv* env, jobject, jint width, jint height, jbyteArray yuv, jintArray bgra)
{
	try {
		jbyte* _yuv = env->GetByteArrayElements(yuv, 0);
		jint* _bgra = env->GetIntArrayElements(bgra, 0);

		Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
		Mat mbgra(height, width, CV_8UC4, (unsigned char *)_bgra);
		Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

		cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);

		env->ReleaseIntArrayElements(bgra, _bgra, 0);
		env->ReleaseByteArrayElements(yuv, _yuv, 0);
	} catch(cv::Exception e)
	{
		LOGD("nativeCreateObject catched cv::Exception: %s", e.what());
		jclass je = env->FindClass("org/opencv/core/CvException");
		if(!je)
		je = env->FindClass("java/lang/Exception");
		env->ThrowNew(je, e.what());
	}
	catch (...)
	{
		LOGD("nativeCreateObject catched unknown exception");
		jclass je = env->FindClass("java/lang/Exception");
		env->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
	}


}
JNIEXPORT void JNICALL Java_org_opencv_face_Sample3View_FindFaces(JNIEnv* env, jobject, jstring jImageName,jstring jFileName)
{
	try {

		const char* jnamestr = env->GetStringUTFChars(jFileName, NULL);
		string stdFileName(jnamestr);
		const char* jimagestr = env->GetStringUTFChars(jImageName, NULL);
		string stdImageName(jimagestr);

		LOGD("Image = %s ",stdImageName.c_str());

		CascadeClassifier haar_cascade;
		if(haar_cascade.load(stdFileName)){
			LOGD("Haar successfully loaded");
		}
		LOGD("cascade : %s ",stdFileName.c_str());
		Mat original = imread(stdImageName+".jpg", 1);
		if(! original.data )                       // Check for invalid input
		{
		    LOGD("Could not load image");
		}
		Mat gray ;//= original.clone();
		cvtColor(original,gray,CV_BGR2GRAY);
		vector< Rect_<int> > faces;
		haar_cascade.detectMultiScale(gray, faces, 1.1, 3, 0, Size(20,60));
		LOGD("FACES : %d ",faces.size());
		if(faces.size()>0) {
			Rect face_i = faces[0];
			Mat original_face = original(face_i);
			resize(original_face,original_face,Size(200,200));
			imwrite(stdImageName+"_det.jpg",original_face);
		}

		return;
	} catch(cv::Exception e)
	{
		LOGD("nativeCreateObject catched cv::Exception: %s", e.what());
		jclass je = env->FindClass("org/opencv/core/CvException");
		if(!je)
		je = env->FindClass("java/lang/Exception");
		env->ThrowNew(je, e.what());
	}
	catch (...)
	{
		LOGD("nativeCreateObject catched unknown exception");
		jclass je = env->FindClass("java/lang/Exception");
		env->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
	}

}

JNIEXPORT int JNICALL Java_org_opencv_face_Sample3View_Find(JNIEnv* env, jobject, jstring jImageName,jstring jFileName,jstring jCsv)
{
	try {

		const char* jnamestr = env->GetStringUTFChars(jFileName, NULL);
		string stdFileName(jnamestr);
		const char* jimagestr = env->GetStringUTFChars(jImageName, NULL);
		string stdImageName(jimagestr);
		const char* jCsvstr = env->GetStringUTFChars(jCsv, NULL);
		string stdCsv(jCsvstr);

		vector<Mat> images;
		vector<int> labels;
		std::ifstream file(stdCsv.c_str(), ifstream::in);
		// vector<KeyPoint> v;

		LOGD("Image = %s ",stdImageName.c_str());

		string line, path, classlabel;
		while (getline(file, line)) {
			stringstream liness(line);
			getline(liness, path, ';');
			getline(liness, classlabel);
			if(!path.empty() && !classlabel.empty()) {
				images.push_back(imread(path, 0));
				labels.push_back(atoi(classlabel.c_str()));
			}
		}

		int im_width = images[0].cols;
		int im_height = images[0].rows;
		// Create a FaceRecognizer and train it on the given images:
		Ptr<FaceRecognizer> model = createEigenFaceRecognizer();
		model->train(images, labels);

		CascadeClassifier haar_cascade;
		haar_cascade.load(stdFileName);
		for(;;)
		{
			Mat original = imread(stdImageName+".jpg", 1);
			Mat gray = original.clone();
			cvtColor(original,gray,CV_BGR2GRAY);
			vector< Rect_<int> > faces;
			haar_cascade.detectMultiScale(gray, faces, 1.1, 3, 0, Size(20,60));
			LOGD("No of faces = %d",faces.size());
			for(int i = 0; i < faces.size(); i++) {
				// Process face by face:
				Rect face_i = faces[i];
				// Crop the face from the image.
				Mat face = gray(face_i);
				Mat face_resized;
				cv::resize(face, face_resized, Size(im_width, im_height), 1.0, 1.0, INTER_CUBIC);
				// Now perform the prediction
				double predicted_confidence = 0.0;
				int prediction;
				model->predict(face_resized,prediction,predicted_confidence);
				LOGD("Prediction = %d Predicted Confidence = %Lf",prediction,predicted_confidence);
				if(prediction>=0)
					return prediction;
//				else
//					return -1;
			}
			return -1;
		}

	} catch(cv::Exception e)
	{
		LOGD("nativeCreateObject catched cv::Exception: %s", e.what());
		jclass je = env->FindClass("org/opencv/core/CvException");
		if(!je)
		je = env->FindClass("java/lang/Exception");
		env->ThrowNew(je, e.what());
	}
	catch (...)
	{
		LOGD("nativeCreateObject catched unknown exception");
		jclass je = env->FindClass("java/lang/Exception");
		env->ThrowNew(je, "Unknown exception in JNI code {highgui::VideoCapture_n_1VideoCapture__()}");
	}

}

}
