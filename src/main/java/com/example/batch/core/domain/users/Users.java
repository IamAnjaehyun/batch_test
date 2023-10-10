package com.example.batch.core.domain.users;

import com.example.batch.core.domain.users.type.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Entity
@ToString
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Status status;
    @CreatedDate
    private LocalDate createdAt;
    @LastModifiedDate
    private LocalDate modifiedAt;


    public Users(Users users) {
        this.id = users.getId();
        this.name = users.getName();
        this.phoneNumber = users.getPhoneNumber();
        this.status = Status.NEW_USER;
        this.createdAt = users.getCreatedAt();
        this.modifiedAt = users.getModifiedAt();
    }


    public void setStatus(Status status) {
        this.status = status;
    }
}
