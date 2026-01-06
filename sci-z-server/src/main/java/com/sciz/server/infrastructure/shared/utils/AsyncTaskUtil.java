package com.sciz.server.infrastructure.shared.utils;

import com.sciz.server.domain.pojo.dto.response.user.LoginUserContext;
import com.sciz.server.infrastructure.shared.context.AsyncUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 异步任务工具类
 * <p>
 * 用于在事务提交后执行异步任务，并自动处理用户上下文传递
 * <p>
 * <strong>使用场景：</strong>
 * <ul>
 * <li>事务提交后需要执行异步任务</li>
 * <li>异步任务中需要访问用户上下文（LoginUserUtil、DataPermissionUtil）</li>
 * <li>需要确保数据已提交到数据库后再执行异步任务</li>
 * </ul>
 * <p>
 * <strong>工作原理：</strong>
 * <ul>
 * <li>如果当前有活跃事务，使用 TransactionSynchronization 在事务提交后执行</li>
 * <li>如果没有活跃事务，直接执行异步任务</li>
 * <li>自动设置和清理 AsyncUserContext，确保异步线程中能正常获取用户信息</li>
 * </ul>
 * <p>
 * <strong>使用示例：</strong>
 * 
 * <pre>
 * {@code
 * // 在事务方法中
 * var currentUser = LoginUserUtil.requireCurrentUser();
 * 
 * // 提交异步任务（事务提交后执行）
 * AsyncTaskUtil.executeAfterCommit(() -> {
 *     // 异步任务逻辑
 *     doSomething();
 * }, currentUser, globalTaskExecutor);
 * }
 * </pre>
 *
 * @author JiaWen.Wu
 * @className AsyncTaskUtil
 * @date 2026-01-06 11:30
 */
@Slf4j
public final class AsyncTaskUtil {

    private AsyncTaskUtil() {
    }

    /**
     * 在事务提交后执行异步任务（自动处理用户上下文）
     * <p>
     * 如果当前有活跃事务，会在事务提交后执行；如果没有活跃事务，会立即执行
     *
     * @param task        异步任务（Runnable）
     * @param currentUser 当前登录用户上下文（用于在异步线程中传递用户信息）
     * @param executor    执行器（线程池）
     */
    public static void executeAfterCommit(Runnable task, LoginUserContext currentUser, Executor executor) {
        if (task == null) {
            log.warn("异步任务为空，跳过执行");
            return;
        }

        if (currentUser == null) {
            log.warn("用户上下文为空，异步任务可能无法正常获取用户信息");
        }

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 如果有活跃事务，在事务提交后执行
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executeAsyncTask(task, currentUser, executor, "事务提交后");
                }
            });
            log.debug("已注册事务提交后的异步任务");
        } else {
            // 如果没有活跃事务，直接执行异步任务
            executeAsyncTask(task, currentUser, executor, "无事务");
        }
    }

    /**
     * 在事务提交后执行异步任务并返回结果（自动处理用户上下文）
     * <p>
     * 如果当前有活跃事务，会在事务提交后执行；如果没有活跃事务，会立即执行
     *
     * @param supplier    异步任务（Supplier，返回结果）
     * @param currentUser 当前登录用户上下文（用于在异步线程中传递用户信息）
     * @param executor    执行器（线程池）
     * @param <T>         返回类型
     * @return CompletableFuture 异步任务结果
     */
    public static <T> CompletableFuture<T> executeAfterCommit(Supplier<T> supplier, LoginUserContext currentUser,
            Executor executor) {
        if (supplier == null) {
            log.warn("异步任务为空，跳过执行");
            return CompletableFuture.completedFuture(null);
        }

        if (currentUser == null) {
            log.warn("用户上下文为空，异步任务可能无法正常获取用户信息");
        }

        CompletableFuture<T> future = new CompletableFuture<>();

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // 如果有活跃事务，在事务提交后执行
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    executeAsyncTaskWithResult(supplier, currentUser, executor, future, "事务提交后");
                }
            });
            log.debug("已注册事务提交后的异步任务（带返回值）");
        } else {
            // 如果没有活跃事务，直接执行异步任务
            executeAsyncTaskWithResult(supplier, currentUser, executor, future, "无事务");
        }

        return future;
    }

    /**
     * 执行异步任务（内部方法）
     *
     * @param task        异步任务
     * @param currentUser 用户上下文
     * @param executor    执行器
     * @param context     上下文描述（用于日志）
     */
    private static void executeAsyncTask(Runnable task, LoginUserContext currentUser, Executor executor,
            String context) {
        CompletableFuture.runAsync(() -> {
            try {
                // 设置异步用户上下文
                if (currentUser != null) {
                    AsyncUserContext.set(currentUser);
                }

                // 执行任务
                task.run();
            } catch (Exception e) {
                log.error(String.format("异步任务执行失败（%s）: err=%s", context, e.getMessage()), e);
            } finally {
                // 清理异步用户上下文（防止内存泄漏）
                AsyncUserContext.clear();
            }
        }, executor);
        log.debug(String.format("已提交异步任务（%s）", context));
    }

    /**
     * 执行异步任务并返回结果（内部方法）
     *
     * @param supplier    异步任务
     * @param currentUser 用户上下文
     * @param executor    执行器
     * @param future      结果 Future
     * @param context     上下文描述（用于日志）
     * @param <T>         返回类型
     */
    private static <T> void executeAsyncTaskWithResult(Supplier<T> supplier, LoginUserContext currentUser,
            Executor executor, CompletableFuture<T> future, String context) {
        CompletableFuture.supplyAsync(() -> {
            try {
                // 设置异步用户上下文
                if (currentUser != null) {
                    AsyncUserContext.set(currentUser);
                }

                // 执行任务并返回结果
                return supplier.get();
            } catch (Exception e) {
                log.error(String.format("异步任务执行失败（%s）: err=%s", context, e.getMessage()), e);
                future.completeExceptionally(e);
                return null;
            } finally {
                // 清理异步用户上下文（防止内存泄漏）
                AsyncUserContext.clear();
            }
        }, executor).whenComplete((result, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(throwable);
            } else {
                future.complete(result);
            }
        });
        log.debug(String.format("已提交异步任务（%s，带返回值）", context));
    }
}
