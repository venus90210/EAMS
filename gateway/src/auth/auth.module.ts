import { Module } from '@nestjs/common';
import { JwtStrategy } from './jwt.strategy';
import { JwtAuthGuard } from './jwt-auth.guard';
import { AuthController } from './auth.controller';
import { ProxyModule } from '../proxy/proxy.module';

@Module({
  imports: [ProxyModule],
  controllers: [AuthController],
  providers: [JwtStrategy, JwtAuthGuard],
  exports: [JwtStrategy, JwtAuthGuard],
})
export class AuthModule {}
