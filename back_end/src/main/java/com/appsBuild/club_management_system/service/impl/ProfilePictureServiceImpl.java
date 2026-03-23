package com.appsBuild.club_management_system.service.impl;

import com.appsBuild.club_management_system.exception.impl.NoContentException;
import com.appsBuild.club_management_system.exception.impl.NotFoundException;
import com.appsBuild.club_management_system.model.entity.ProfilePicture;
import com.appsBuild.club_management_system.repository.ProfilePictureRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfilePictureServiceImpl {
  private final ProfilePictureRepository profilePictureRepository;

  public ProfilePicture save(ProfilePicture profilePicture) {
    if (profilePicture == null) {
      throw new NoContentException("ProfilePicture cannot be null");
    }
    return profilePictureRepository.save(profilePicture);
  }

  public ProfilePicture findById(Long id) {
    return profilePictureRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("ProfilePicture not found with id: " + id));
  }

  public Optional<ProfilePicture> findByIdOptional(Long id) {
    return profilePictureRepository.findById(id);
  }

  public List<ProfilePicture> findAll() {
    List<ProfilePicture> profilePictures = profilePictureRepository.findAll();
    if (profilePictures.isEmpty()) {
      throw new NoContentException("No profile pictures found");
    }
    return profilePictures;
  }

  public void delete(Long id) {
    ProfilePicture profilePicture = findById(id);
    profilePictureRepository.delete(profilePicture);
  }
}
