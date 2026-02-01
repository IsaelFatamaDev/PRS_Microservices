package pe.edu.vallegrande.vgmsusers.application.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.application.service.UserCodeService;
import pe.edu.vallegrande.vgmsusers.domain.models.UserCodeCounter;
import pe.edu.vallegrande.vgmsusers.infrastructure.document.UserCodeCounterDocument;
import pe.edu.vallegrande.vgmsusers.infrastructure.mapper.UserCodeCounterMapper;
import pe.edu.vallegrande.vgmsusers.infrastructure.repository.UserCodeCounterRepository;
import reactor.core.publisher.Mono;

@Service
public class UserCodeServiceImpl implements UserCodeService {

     private final UserCodeCounterRepository userCodeCounterRepository;
     private final UserCodeCounterMapper userCodeCounterMapper;

     public UserCodeServiceImpl(UserCodeCounterRepository userCodeCounterRepository,
               UserCodeCounterMapper userCodeCounterMapper) {
          this.userCodeCounterRepository = userCodeCounterRepository;
          this.userCodeCounterMapper = userCodeCounterMapper;
     }

     @Override
     public Mono<String> generateUserCode(String organizationId) {
          return userCodeCounterRepository.findByOrganizationId(organizationId)
                    .map(userCodeCounterMapper::toDomain)
                    .switchIfEmpty(createNewCounter(organizationId))
                    .flatMap(this::incrementAndSave)
                    .map(counter -> String.format("%s%05d", counter.getPrefix(), counter.getLastCode()));
     }

     @Override
     public Mono<String> getNextUserCode(String organizationId) {
          return userCodeCounterRepository.findByOrganizationId(organizationId)
                    .map(userCodeCounterMapper::toDomain)
                    .switchIfEmpty(createNewCounter(organizationId))
                    .map(UserCodeCounter::getNextCode);
     }

     @Override
     public Mono<String> getLastUserCode(String organizationId) {
          return userCodeCounterRepository.findByOrganizationId(organizationId)
                    .map(userCodeCounterMapper::toDomain)
                    .map(counter -> {
                         if (counter.getLastCode() == null || counter.getLastCode() == 0) {
                              return "Ningún código generado aún";
                         }
                         return String.format("%s%05d", counter.getPrefix(), counter.getLastCode());
                    })
                    .defaultIfEmpty("Ningún código generado aún");
     }

     @Override
     public Mono<Void> resetCounter(String organizationId) {
          return userCodeCounterRepository.findByOrganizationId(organizationId)
                    .flatMap(document -> {
                         document.setLastCode(0L);
                         return userCodeCounterRepository.save(document);
                    })
                    .then();
     }

     private Mono<UserCodeCounter> createNewCounter(String organizationId) {
          UserCodeCounter newCounter = UserCodeCounter.builder()
                    .organizationId(organizationId)
                    .lastCode(0L)
                    .prefix("USR")
                    .build();

          UserCodeCounterDocument document = userCodeCounterMapper.toDocument(newCounter);
          return userCodeCounterRepository.save(document)
                    .map(userCodeCounterMapper::toDomain);
     }

     private Mono<UserCodeCounter> incrementAndSave(UserCodeCounter counter) {
          counter.setLastCode(counter.getLastCode() + 1);
          UserCodeCounterDocument document = userCodeCounterMapper.toDocument(counter);
          return userCodeCounterRepository.save(document)
                    .map(userCodeCounterMapper::toDomain);
     }
}