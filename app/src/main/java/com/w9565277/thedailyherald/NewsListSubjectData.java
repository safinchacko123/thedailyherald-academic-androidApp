package com.w9565277.thedailyherald;

public class NewsListSubjectData {
    String SubjectName;
    String Image;
    int ImageWidth;
    int ImageHeight;

    public NewsListSubjectData(String subjectName, String image,int ImageWidth,int ImageHeight) {
        this.SubjectName = subjectName;
        this.Image = image;
        this.ImageWidth = ImageWidth;
        this.ImageHeight = ImageHeight;
    }
}
