package org.sventon.service.javahl;

import org.sventon.model.ChangeType;
import org.sventon.model.ChangedPath;
import org.sventon.model.RevisionProperty;
import org.tigris.subversion.javahl.ChangePath;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.RevisionRange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JavaHLConverter {


  static Revision convertRevision(long toRevision) {
    return Revision.getInstance(toRevision);
  }

  static Set<ChangedPath> convertChangedPaths(ChangePath[] changePaths) {
    final HashSet<ChangedPath> changedPaths = new HashSet<ChangedPath>();

    for (ChangePath cp : changePaths) {
      changedPaths.add(new ChangedPath(cp.getPath(), cp.getCopySrcPath(), cp.getCopySrcRevision(), ChangeType.parse(cp.getAction())));
    }

    return changedPaths;
  }

  static Map<RevisionProperty, String> convertRevisionPropertyMap(Map map) {
    final HashMap<RevisionProperty, String> propertyMap = new HashMap<RevisionProperty, String>();

    if (map != null){
      for (Object o : map.keySet()) {
        String property = (String) o;
        propertyMap.put(RevisionProperty.byName(property), (String) map.get(property));

      }
    }

    return propertyMap;
  }

  static RevisionRange[] getRevisionRange(long fromRevision, long toRevision) {
    return new RevisionRange[]{new RevisionRange(convertRevision(fromRevision), convertRevision(toRevision))};
  }
}