package com.modelcity.leisure.events.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Soft-deletes an event and automatically refunds every outstanding ticket. Admin only.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDeleteEventUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DeleteEventUseCase {

    void execute(Long id, String sub);
}
