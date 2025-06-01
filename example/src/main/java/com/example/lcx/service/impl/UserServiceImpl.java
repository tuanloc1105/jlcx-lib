package com.example.lcx.service.impl;

import com.example.lcx.entity.UserEntity;
import com.example.lcx.enums.AppError;
import com.example.lcx.mapper.UserMapper;
import com.example.lcx.object.dto.UserDTO;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.object.response.UserLoginResponse;
import com.example.lcx.respository.UserRepository;
import com.example.lcx.service.UserService;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.utils.BCryptUtils;
import vn.com.lcx.jpa.annotation.Service;
import vn.com.lcx.jpa.annotation.Transactional;
import vn.com.lcx.jpa.exception.JpaException;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.sql.Connection;
import java.util.Optional;

@Service
@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SessionFactory sessionFactory;
    private final UserMapper userMapper;
    private final JWTAuth jwtAuth;
    private final Gson gson;

    @Transactional(onRollback = {InternalServiceException.class, JpaException.class})
    public void createNew(final CreateNewUserRequest request) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.doWork(connection ->
                    connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE)
            );
            final var query = session.createQuery(
                    "select u from UserEntity u where u.username = :username",
                    UserEntity.class
            );
            query.setParameter("username", request.getUsername());
            if (Optional.ofNullable(query.uniqueResult()).isPresent()) {
                throw new InternalServiceException(AppError.USER_EXISTED);
            }
            UserEntity user = new UserEntity();
            user.setUsername(request.getUsername());
            user.setPassword(BCryptUtils.hashPassword(request.getPassword()));
            user.setFullName(request.getFullName());
            session.persist(user);
            transaction.commit();
        } catch (InternalServiceException e) {
            throw e;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException(e);
        }
    }

    public UserLoginResponse login(final UserLoginRequest request) {
        try (Session session = sessionFactory.openSession()) {
            final HibernateCriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            final CriteriaQuery<UserEntity> criteriaQuery = criteriaBuilder.createQuery(UserEntity.class);
            Root<UserEntity> root = criteriaQuery.from(UserEntity.class);
            criteriaQuery.select(root);
            Predicate usernamePredicate = criteriaBuilder.equal(root.get("username"), request.getUsername());
            criteriaQuery.where(usernamePredicate);
            Query<UserEntity> query = session.createQuery(criteriaQuery);
            var user = Optional.ofNullable(query.uniqueResult()).orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
            BCryptUtils.comparePassword(request.getPassword(), user.getPassword());
            final var tokenInfo = UserJWTTokenInfo.builder()
                    .id(user.getId().longValue())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .build();
            String token = this.jwtAuth.generateToken(
                    new JsonObject(this.gson.toJson(tokenInfo)),
                    new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(14400)
            );
            return new UserLoginResponse(token, user.getFullName());
        } catch (InternalServiceException e) {
            throw e;
        }
    }

    public UserDTO getUserByUsername(final String username) {
        return userMapper.map(userRepository.findByUsernameAndActive(username, true));
    }

}
