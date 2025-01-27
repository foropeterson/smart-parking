package com.parking.com.smart.parking.repository;

import com.parking.com.smart.parking.entities.AppRole;
import com.parking.com.smart.parking.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(AppRole appRole);
}
