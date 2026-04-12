import { Injectable, UnauthorizedException } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { ConfigService } from '@nestjs/config';
import { Strategy, ExtractJwt } from 'passport-jwt';

export interface JwtPayload {
  sub: string;
  role: string;
  institutionId?: string;
  mfaPending?: boolean;
}

export interface ValidatedUser {
  userId: string;
  role: string;
  institutionId?: string;
}

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(private configService: ConfigService) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: configService.get<string>('JWT_SECRET'),
    });
  }

  validate(payload: JwtPayload): ValidatedUser {
    if (payload.mfaPending === true) {
      throw new UnauthorizedException('MFA verification required');
    }

    return {
      userId: payload.sub,
      role: payload.role,
      institutionId: payload.institutionId,
    };
  }
}
