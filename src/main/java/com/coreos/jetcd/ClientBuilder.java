package com.coreos.jetcd;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.exception.AuthFailedException;
import com.coreos.jetcd.exception.ConnectException;
import com.coreos.jetcd.exception.EtcdExceptionFactory;
import com.coreos.jetcd.internal.impl.ClientImpl;
import com.google.common.collect.Lists;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolver;
import java.util.List;

/**
 * ClientBuilder knows how to create an Client instance.
 */
public class ClientBuilder implements Cloneable {

  private List<String> endpoints = Lists.newArrayList();
  private ByteSequence user;
  private ByteSequence password;
  private NameResolver.Factory nameResolverFactory;
  private ManagedChannelBuilder<?> channelBuilder;
  private boolean lazyInitialization = false;

  private ClientBuilder() {
  }

  public static ClientBuilder newBuilder() {
    return new ClientBuilder();
  }

  /**
   * gets the endpoints for the builder.
   *
   * @return the list of endpoints configured for the builder
   */
  public List<String> getEndpoints() {
    return this.endpoints;
  }

  /**
   * configure etcd server endpoints.
   *
   * @param endpoints etcd server endpoints, at least one
   * @return this builder to train
   * @throws NullPointerException if endpoints is null or one of endpoint is null
   * @throws IllegalArgumentException if endpoints is empty or some endpoint is invalid
   */
  public ClientBuilder setEndpoints(String... endpoints) {
    checkNotNull(endpoints, "endpoints can't be null");
    checkArgument(endpoints.length > 0, "please configure at lease one endpoint ");

    // TODO: check endpoint is in host:port format
    for (String endpoint : endpoints) {
      checkNotNull(endpoint, "endpoint can't be null");
      final String trimmedEndpoint = endpoint.trim();
      checkArgument(trimmedEndpoint.length() > 0, "invalid endpoint: endpoint=" + endpoint);
      this.endpoints.add(trimmedEndpoint);
    }
    return this;
  }

  public ByteSequence getUser() {
    return user;
  }

  /**
   * config etcd auth name.
   *
   * @param user etcd auth user
   * @return this builder
   * @throws NullPointerException if name is null
   */
  public ClientBuilder setUser(ByteSequence user) {
    checkNotNull(user, "user can't be null");
    this.user = user;
    return this;
  }

  public ByteSequence getPassword() {
    return password;
  }

  /**
   * config etcd auth password.
   *
   * @param password etcd auth password
   * @return this builder
   * @throws NullPointerException if password is null
   */
  public ClientBuilder setPassword(ByteSequence password) {
    checkNotNull(password, "password can't be null");
    this.password = password;
    return this;
  }

  /**
   * config etcd auth password.
   *
   * @param nameResolverFactory etcd nameResolverFactory
   * @return this builder
   * @throws NullPointerException if password is null
   */
  public ClientBuilder setNameResolverFactory(NameResolver.Factory nameResolverFactory) {
    checkNotNull(nameResolverFactory);
    this.nameResolverFactory = nameResolverFactory;
    return this;
  }

  /**
   * get nameResolverFactory for etcd client.
   *
   * @return nameResolverFactory
   */
  public NameResolver.Factory getNameResolverFactory() {
    return nameResolverFactory;
  }

  public ManagedChannelBuilder<?> getChannelBuilder() {
    return channelBuilder;
  }

  public ClientBuilder setChannelBuilder(ManagedChannelBuilder<?> channelBuilder) {
    this.channelBuilder = channelBuilder;
    return this;
  }

  public boolean isLazyInitialization() {
    return lazyInitialization;
  }

  /**
   * Define if the client has to initialize connectivity and authentication on client constructor
   * or delay it to the first call to a client. Default is false.
   *
   * @param lazyInitialization true if the client has to lazily perform connectivity/authentication.
   * @return this builder
   */
  public ClientBuilder setLazyInitialization(boolean lazyInitialization) {
    this.lazyInitialization = lazyInitialization;
    return this;
  }

  /**
   * build a new Client.
   *
   * @return Client instance.
   * @throws ConnectException As network reason, wrong address
   * @throws AuthFailedException This may be caused as wrong username or password
   */
  public Client build() {
    checkState(!endpoints.isEmpty() || nameResolverFactory != null,
        "please configure etcd server endpoints or nameResolverFactory before build.");
    return new ClientImpl(this);
  }

  public ClientBuilder copy() {
    try {
      return (ClientBuilder)super.clone();
    } catch (CloneNotSupportedException e) {
      throw EtcdExceptionFactory.newEtcdException(e);
    }
  }
}
