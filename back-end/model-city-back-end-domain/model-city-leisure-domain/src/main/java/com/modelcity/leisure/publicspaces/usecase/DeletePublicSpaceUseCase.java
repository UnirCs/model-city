package com.modelcity.leisure.publicspaces.usecase;


import com.modelcity.common.extensibility.ModelCityExtensionPoint;
/**
 * Soft-deletes a public space and cascades the soft-delete to its resources. Admin only.
 *
 * <p>Extension point: override by declaring a {@code @Service} bean implementing this interface;
 * the {@link DefaultDeletePublicSpaceUseCase} default then backs off.
 */
@ModelCityExtensionPoint
public interface DeletePublicSpaceUseCase {

    void execute(Long id, String sub);
}
