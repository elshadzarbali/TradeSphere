package com.tradesphere.walletservice.mapper;

import com.tradesphere.walletservice.domain.entity.Wallet;
import com.tradesphere.walletservice.dto.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

// Setting componentModel to "spring" generates an implementation annotated with @Component,
// allowing it to be injected into services or controllers via constructor injection.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WalletMapper {

    WalletResponse toDto(Wallet wallet);

    // TODO: not map correctly, time is before current time
    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
