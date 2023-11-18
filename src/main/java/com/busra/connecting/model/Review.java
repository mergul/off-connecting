package com.busra.connecting.model;

import com.busra.connecting.model.serdes.ReviewDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

//@JsonDeserialize(using = ReviewDeserializer.class)
public class Review {
    private String doc_name;
    private String doc_description;
    private String file_name;
    private String file_type;
    private Boolean has_medium;

    public  Review(){}

    public Review(String doc_name,String doc_description,String file_name,String file_type, Boolean has_medium){
        this.doc_name=doc_name;
        this.doc_description=doc_description;
        this.file_name=file_name;
        this.file_type=file_type;
        this.has_medium=has_medium;
    }
    @Override
    public String toString() {
        return "Review{" +
                "doc_name='" + doc_name + '\'' +
                ", doc_description='" + doc_description + '\'' +
                ", file_name=" + file_name +
                ", file_type=" + file_type +
                ", has_medium=" + has_medium +
                '}';
    }
    public String getDoc_name() {
        return doc_name;
    }

    public void setDoc_name(String doc_name) {
        this.doc_name = doc_name;
    }

    public String getDoc_description() {
        return doc_description;
    }

    public void setDoc_description(String doc_description) {
        this.doc_description = doc_description;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }
    public Boolean getHas_medium() {
        return has_medium;
    }

    public void setHas_medium(Boolean has_medium) {
        this.has_medium = has_medium;
    }

}
