package com.w9565277.thedailyherald;

public class NewsListSubjectData {
    String SubjectName;
    String Image;
    int ImageWidth;
    int ImageHeight;
    String NewsUrl;

    public NewsListSubjectData(String subjectName, String image,int ImageWidth,int ImageHeight,String newsUrl) {
        this.SubjectName = subjectName;
        this.Image = image;
        this.ImageWidth = ImageWidth;
        this.ImageHeight = ImageHeight;
        this.NewsUrl = newsUrl;
    }
}
