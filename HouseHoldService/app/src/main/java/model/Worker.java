package model;


import java.util.List;

public class Worker {
    private String id;
    private String name;
    private String email;
    private String phone;
    private List<String> servicesOffered;
    private float rating;
    private boolean available;


    public Worker(String id, String name, String email, String phone,
                  List<String> servicesOffered, float rating, boolean available) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.servicesOffered = servicesOffered;
        this.rating = rating;
        this.available = available;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getServicesOffered() {
        return servicesOffered;
    }

    public void setServicesOffered(List<String> servicesOffered) {
        this.servicesOffered = servicesOffered;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
