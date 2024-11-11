package com.infra.authorization.controller.dto;

/**
 * Created by rajeevkumarsingh on 02/08/17.
 */

public class UpdateImageRequest {
    private String imageUrl;
    private String name;
    private String phone;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
