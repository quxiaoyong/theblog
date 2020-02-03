package org.fantasizer.theblog.picture.service.impl;

import org.fantasizer.common.service.impl.BaseServiceImpl;
import org.fantasizer.theblog.picture.entity.File;
import org.fantasizer.theblog.picture.mapper.FileMapper;
import org.fantasizer.theblog.picture.service.FileService;
import org.springframework.stereotype.Service;
@Service
public class FileServiceImpl extends BaseServiceImpl<FileMapper, File> implements FileService {

}
